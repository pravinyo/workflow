package dev.pravin.workflow.lamf;

public class Constants {
    // data attributes
    public static final String DA_APPLICATION_DETAILS = "da_application_details";
    public static final String DA_VALIDATE_OTP_ATTEMPT = "da_validate_otp_attempt";
    public static final String DA_CURRENT_STEP = "da_current_step";

    // signal channel
    public static final String IC_USER_INPUT_CONSENT = "ic_user_input_consent";
    public static final String SC_USER_INPUT_MF_PULL_OTP = "sc_user_input_mf_pull_otp";
    public static final String SC_USER_INPUT_MF_SCHEME_LIST = "sc_user_input_mf_scheme_list";
    public static final String SC_USER_INPUT_KYC = "sc_user_input_kyc";
    public static final String SC_AADHAAR_OTP_SIGNAL = "sc_aadhaar_otp_signal";
    public static final String IC_SYSTEM_KYC_COMPLETED = "ic_system_kyc_completed";

    //search attributes
    public static final String SA_CUSTOMER_ID = "customer_id";
    public static final String SA_PARENT_WORKFLOW_ID = "parentWorkflowId";

    // KYC STATE
    public static final String KYC_SUCCESS = "SUCCESS";
    public static final String KYC_FAILED = "FAILED";
    public static final String OTP_ATTEMPT = "otp_attempt";
    public static final String KYC_STATUS = "kyc_status";
}
