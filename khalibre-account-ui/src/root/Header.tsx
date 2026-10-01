import { useEnvironment } from "@keycloak/keycloak-account-ui";
import { KeycloakMasthead, label } from "@keycloak/keycloak-ui-shared";
import { Button, PageContext } from "@patternfly/react-core";
import { ExternalLinkSquareAltIcon } from "@patternfly/react-icons";
import { MouseEvent, useContext } from "react";
import { useTranslation } from "react-i18next";
import { useHref } from "react-router-dom";

import { environment } from "../environment";
import { joinPath } from "../utils/joinPath";

import style from "./header.module.css";

const ReferrerLink = () => {
  const { t } = useTranslation();

  return environment.referrerUrl ? (
    <Button
      data-testid="referrer-link"
      component="a"
      href={environment.referrerUrl.replace("_hash_", "#")}
      variant="link"
      icon={<ExternalLinkSquareAltIcon />}
      iconPosition="right"
      isInline
    >
      {t("backTo", {
        app: label(t, environment.referrerName, environment.referrerUrl),
      })}
    </Button>
  ) : null;
};

export const Header = () => {
  const { environment, keycloak } = useEnvironment();
  const { t } = useTranslation();
  const { onSidebarToggle } = useContext(PageContext);

  const brandImage = environment.logo || "logo.svg";
  const logoUrl = environment.logoUrl ? environment.logoUrl : "/";
  const internalLogoHref = useHref(logoUrl);

  // User can indicate that he wants an internal URL by starting it with "/"
  const indexHref = logoUrl.startsWith("/") ? internalLogoHref : logoUrl;

  const handleClickCapture = (event: MouseEvent<HTMLDivElement>) => {
    const target = event.target as HTMLElement;
    if (target.closest('#nav-toggle')) {
      onSidebarToggle();
    }
  };

  return (
    <KeycloakMasthead
      data-testid="page-header"
      keycloak={keycloak}
      features={{ hasManageAccount: false }}
      brand={{
        href: indexHref,
        src: brandImage.startsWith("/")
          ? joinPath(environment.resourceUrl, brandImage)
          : brandImage,
        alt: t("logo"),
        className: style.brand,
      }}
      toolbarItems={[<ReferrerLink key="link" />]}
      onClickCapture={ handleClickCapture }
    />
  );
};