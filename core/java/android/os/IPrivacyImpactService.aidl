package android.os;

/**
* {@hide}
*/
interface IPrivacyImpactService {
    boolean showPackagePrivacy(String packageName);
    void disablePackagePrivacy(String packageName);
}
