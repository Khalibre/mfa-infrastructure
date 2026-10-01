import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AccountEnvironment, Page, useAccountAlerts, useEnvironment } from "@keycloak/keycloak-account-ui";
import { ToggleGroup, ToggleGroupItem } from "@patternfly/react-core";
import {
  fetchAccountActivities,
  type AccountActivity,
} from "../api/accountActivities";
import { EventsTable } from "./EventsTable";
import { isErrorActivity } from "../utils/events";

const PAGE_SIZE = 25;

export const AccountActivities = () => {
  const { t } = useTranslation();
  const context = useEnvironment<AccountEnvironment>();
  const { addError } = useAccountAlerts();

  const [page, setPage] = useState(1);
  const [onlyErrors, setOnlyErrors] = useState(false);
  const [activities, setActivities] = useState<AccountActivity[]>();
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);

    fetchAccountActivities(context, {
      first: (page - 1) * PAGE_SIZE,
      max: PAGE_SIZE,
      signal: controller.signal,
    })
      .then((data) => {
        setActivities(data.events);
        // A short page means there is nothing after it.
        setHasMore(data.events.length === PAGE_SIZE);
      })
      .catch((e) => {
        if (e instanceof DOMException && e.name === "AbortError") {
          return;
        }
        setActivities([]);
        addError(t("accountActivitiesLoadError"), e);
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      });

    return () => controller.abort();
  }, [context, page, t, addError]);

  const visible = onlyErrors
    ? (activities ?? []).filter(isErrorActivity)
    : (activities ?? []);

  const onFilter = useCallback((_event: unknown, selected: boolean) => {
    setOnlyErrors(selected);
  }, []);

  return (
    <Page
      title={t("accountActivities")}
      description={t("accountActivitiesDescription")}
    >
      <ToggleGroup aria-label={t("accountActivitiesFilter")}>
        <ToggleGroupItem
          text={t("accountActivitiesAllEvents")}
          buttonId="all-events"
          isSelected={!onlyErrors}
          onChange={onFilter}
        />
        <ToggleGroupItem
          text={t("accountActivitiesOnlyErrors")}
          buttonId="only-errors"
          isSelected={onlyErrors}
          onChange={onFilter}
        />
      </ToggleGroup>

      <EventsTable
        activities={visible}
        loading={loading}
        page={page}
        pageSize={PAGE_SIZE}
        hasMore={hasMore}
        onPage={setPage}
        emptyLabel={t("accountActivitiesEmpty")}
      />
    </Page>
  );
};
