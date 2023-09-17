package dev.pravin.workflow.lamf.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationDetails {
    private Boolean isConsentProvided;
    private String mfPullOtpReferenceId;
    private SelectedMutualFund selectedMutualFund;
    private LoanDetails loanDetails;
}