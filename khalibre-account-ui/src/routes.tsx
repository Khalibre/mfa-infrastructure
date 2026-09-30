import type { IndexRouteObject, RouteObject } from "react-router-dom";
import { environment } from "./environment";
import {
  Applications,
  ContentComponent,
  DeviceActivity,
  Groups,
  LinkedAccounts,
  Oid4Vci,
  PersonalInfo,
  Resources,
  SigningIn,
} from "@keycloak/keycloak-account-ui";
import { MyPage } from "./MyPage";

export const DeviceActivityRoute: RouteObject = {
  path: "account-security/device-activity",
  element: <DeviceActivity />,
};

export const LinkedAccountsRoute: RouteObject = {
  path: "account-security/linked-accounts",
  element: <LinkedAccounts />,
};

export const SigningInRoute: RouteObject = {
  path: "account-security/signing-in",
  element: <SigningIn />,
};

export const ApplicationsRoute: RouteObject = {
  path: "applications",
  element: <Applications />,
};

export const GroupsRoute: RouteObject = {
  path: "groups",
  element: <Groups />,
};

export const ResourcesRoute: RouteObject = {
  path: "resources",
  element: <Resources />,
};

export type ContentComponentParams = {
  componentId: string;
};

export const ContentRoute: RouteObject = {
  path: "content/:componentId",
  element: <ContentComponent />,
};

export const PersonalInfoRoute: IndexRouteObject = {
  index: true,
  element: <PersonalInfo />,
  path: "",
};

export const Oid4VciRoute: RouteObject = {
  path: "oid4vci",
  element: <Oid4Vci />,
};
export const MyPageRoute: RouteObject = {
  path: "myPage",
  element: <MyPage />,
};

export const routes: RouteObject[] = [
  PersonalInfoRoute,
  DeviceActivityRoute,
  LinkedAccountsRoute,
  SigningInRoute,
  ApplicationsRoute,
  GroupsRoute,
  PersonalInfoRoute,
  ResourcesRoute,
  ContentRoute,
  MyPageRoute,
  ...(environment.features.isOid4VciEnabled ? [Oid4VciRoute] : []),
];