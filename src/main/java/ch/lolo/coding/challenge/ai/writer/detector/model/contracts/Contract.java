package ch.lolo.coding.challenge.ai.writer.detector.model.contracts;

import ch.lolo.coding.challenge.ai.writer.detector.model.Amount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Contract {

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Setter
    @Getter
    private String name;

    @Setter
    @Getter
    private String firstName;

    @Setter
    @Getter
    private String lastName;

    @Setter
    @Getter
    private Amount premium;

}
