package android.os;

/**
* {@hide}
*/
interface IPrivacyImpactService {
    boolean showPackagePrivacy(String packageName);
    void disablePackagePrivacy(String packageName);
    void clearPackagePrivacyData();
    boolean isPrivacyImpactEnabled();
    void setPrivacyImpactStatus(boolean enabled);
}
