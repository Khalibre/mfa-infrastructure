import { AccountEnvironment } from "@keycloak/keycloak-account-ui";
import { getInjectedEnvironment } from "@keycloak/keycloak-ui-shared";

export type Feature = {
    isRegistrationEmailAsUsername: boolean;
    isEditUserNameAllowed: boolean;
    isLinkedAccountsEnabled: boolean;
    isMyResourcesEnabled: boolean;
    deleteAccountAllowed: boolean;
    updateEmailFeatureEnabled: boolean;
    updateEmailActionEnabled: boolean;
    isViewGroupsEnabled: boolean;
    isViewOrganizationsEnabled: boolean;
    isOid4VciEnabled: boolean;
};

export const environment = getInjectedEnvironment<AccountEnvironment>();
