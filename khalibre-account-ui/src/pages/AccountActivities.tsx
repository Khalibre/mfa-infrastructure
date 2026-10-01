import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AccountEnvironment, Page, useAccountAlerts, useEnvironment } from "@keycloak/keycloak-account-ui";
import {
  fetchAccountActivities,
  type AccountActivity,
  type DayFilter,
} from "../api/accountActivities";
import { EventsTable } from "./EventsTable";

const PAGE_SIZE = 25;

export const AccountActivities = () => {
  const { t } = useTranslation();
  const context = useEnvironment<AccountEnvironment>();
  const { addError } = useAccountAlerts();

  const [page, setPage] = useState(1);
  const [dayFilter, setDayFilter] = useState<DayFilter>("30");
  const [activities, setActivities] = useState<AccountActivity[]>();
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);

    fetchAccountActivities(context, {
      first: (page - 1) * PAGE_SIZE,
      max: PAGE_SIZE,
      dayFilter,
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
  }, [context, page, dayFilter, t, addError]);

  const onDayFilterChange = useCallback((nextFilter: DayFilter) => {
    setDayFilter(nextFilter);
    setPage(1);
  }, []);

  return (
    <Page
      title={t("accountActivities")}
      description={t("accountActivitiesDescription")}
    >
      <EventsTable
        activities={activities ?? []}
        loading={loading}
        dayFilter={dayFilter}
        onDayFilter={onDayFilterChange}
        page={page}
        pageSize={PAGE_SIZE}
        hasMore={hasMore}
        onPage={setPage}
      />
    </Page>
  );
};
