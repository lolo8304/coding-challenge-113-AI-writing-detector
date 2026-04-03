#!/bin/bash

# ============================
# ENSURE WORKING DIRECTORY = SCRIPT DIRECTORY
# ============================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || { echo "ERROR: Could not change to script directory: $SCRIPT_DIR"; exit 1; }
echo "Working directory set to: $SCRIPT_DIR"

# ============================
# CHECK DEPENDENCIES
# ============================
if ! command -v openssl &>/dev/null; then
  echo "ERROR: openssl is not installed or not in PATH."
  echo ""
  echo "Please install OpenSSL for Windows:"
  echo "  - Recommended: https://slproweb.com/products/Win32OpenSSL.html"
  echo "    -> Download 'Win64 OpenSSL' (full installer, not Light)"
  echo "  - Alternative via winget: winget install ShiningLight.OpenSSL"
  echo "  - Alternative via Chocolatey: choco install openssl"
  echo ""
  echo "After installation, make sure openssl.exe is on your PATH."
  exit 1
fi

if ! command -v keytool &>/dev/null; then
  echo "ERROR: keytool is not installed or not in PATH."
  echo ""
  echo "keytool is part of the Java JDK/JRE."
  echo "Please install a JDK:"
  echo "  - https://adoptium.net  (Eclipse Temurin)"
  echo "  - Alternative via winget: winget install EclipseAdoptium.Temurin.21.JDK"
  echo ""
  echo "After installation, make sure keytool.exe is on your PATH."
  exit 1
fi

# ============================
# CONFIGURATION
# ============================
DEFAULT_CN="ai-writing-detector"
DEFAULT_OUT_DIR="../certs"

# Ask for output directory
read -rp "Enter output directory (default: ${DEFAULT_OUT_DIR}): " OUT_DIR
OUT_DIR="${OUT_DIR:-$DEFAULT_OUT_DIR}"

# Ask for CN
read -rp "Enter CN (default: ${DEFAULT_CN}): " CN
CN="${CN:-$DEFAULT_CN}"

# Ask for password (twice, non-empty)
while true; do
  read -rsp "Enter keystore password: " PASSWORD
  echo ""
  if [[ -z "$PASSWORD" ]]; then
    echo "ERROR: Password must not be empty. Please try again."
    continue
  fi
  read -rsp "Confirm keystore password: " PASSWORD_CONFIRM
  echo ""
  if [[ "$PASSWORD" == "$PASSWORD_CONFIRM" ]]; then
    break
  fi
  echo "ERROR: Passwords do not match. Please try again."
done

CA_SUBJ="//OU=lolo\\L=Wermatswil\\ST=Zuerich\\C=CH"
CERT_SUBJ="//CN=${CN}\\OU=lolo\\L=Wermatswil\\ST=Zuerich\\C=CH"
SAN="subjectAltName=DNS:${CN},DNS:${CN}.local,DNS:localhost,IP:127.0.0.1"
EKU="extendedKeyUsage=serverAuth,clientAuth"
SAN_FILE="$(mktemp)"
printf "%s\n%s\n" "$SAN" "$EKU" > "$SAN_FILE"

mkdir -p "$OUT_DIR"

echo "Certificates will be stored in: $OUT_DIR"


# ============================
# 1. CREATE CUSTOM ROOT CA
# ============================
if [[ -f "$OUT_DIR/ca.crt" && -f "$OUT_DIR/ca.key" ]]; then
  echo "Root CA already exists, skipping creation."
else
  echo "Creating Root CA..."

  openssl genrsa -out "$OUT_DIR/ca.key" 4096
  echo "created: $OUT_DIR/ca.key"
  echo "---------------------------------------------------------------"

  openssl req -x509 -new -nodes \
    -key "$OUT_DIR/ca.key" \
    -sha256 -days 3650 \
    -subj "$CA_SUBJ" \
    -out "$OUT_DIR/ca.crt"
  echo "created: $OUT_DIR/ca.crt"
  echo "---------------------------------------------------------------"

  echo "Root CA created."
fi


# ============================
# 2. CREATE KEY + CSR
# ============================
echo "Removing existing certificate..."
rm -f "$OUT_DIR/${CN}.key" "$OUT_DIR/${CN}.csr" "$OUT_DIR/${CN}.crt" "$OUT_DIR/${CN}-keystore.p12"

echo "Creating Key and CSR..."

openssl genrsa -out "$OUT_DIR/${CN}.key" 4096
echo "created: $OUT_DIR/${CN}.key"
echo "---------------------------------------------------------------"

openssl req -new \
  -key "$OUT_DIR/${CN}.key" \
  -subj "$CERT_SUBJ" \
  -out "$OUT_DIR/${CN}.csr"
echo "created: $OUT_DIR/${CN}.csr"
echo "---------------------------------------------------------------"


# ============================
# 3. SIGN CERT WITH CA
# ============================
echo "Signing Certificate..."

openssl x509 -req \
  -in "$OUT_DIR/${CN}.csr" \
  -CA "$OUT_DIR/ca.crt" \
  -CAkey "$OUT_DIR/ca.key" \
  -CAcreateserial \
  -out "$OUT_DIR/${CN}.crt" \
  -days 3650 -sha256 \
  -extfile "$SAN_FILE"
echo "created: $OUT_DIR/${CN}.crt"
echo "---------------------------------------------------------------"


# ============================
# 4. CREATE PKCS12 KEYSTORE
# ============================
echo "Creating PKCS12 Keystore..."

openssl pkcs12 -export \
  -in "$OUT_DIR/${CN}.crt" \
  -inkey "$OUT_DIR/${CN}.key" \
  -certfile "$OUT_DIR/ca.crt" \
  -name "${CN}-cert" \
  -out "$OUT_DIR/${CN}-keystore.p12" \
  -keypbe AES-256-CBC \
  -certpbe AES-256-CBC \
  -macalg SHA-256 \
  -password pass:$PASSWORD

echo "created: $OUT_DIR/${CN}-keystore.p12"
echo "---------------------------------------------------------------"


# ============================
# 5. CREATE TRUSTSTORE WITH CA
# ============================
echo "Creating Truststore..."

keytool -importcert \
  -alias rootCA \
  -file "$OUT_DIR/ca.crt" \
  -keystore "$OUT_DIR/truststore.p12" \
  -storetype PKCS12 \
  -storepass "$PASSWORD" \
  -noprompt
echo "created: $OUT_DIR/truststore.p12"
echo "---------------------------------------------------------------"

echo "Truststore created."


# ============================
# DONE
# ============================
echo "All certificates, keystores, and truststores created successfully!"
echo "Location: $OUT_DIR"

# Cleanup
rm -f "$SAN_FILE"
