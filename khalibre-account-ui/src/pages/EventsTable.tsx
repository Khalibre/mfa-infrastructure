import {
  EmptyState,
  EmptyStateBody,
  EmptyStateHeader,
  EmptyStateVariant,
  Label,
  MenuToggle,
  MenuToggleElement,
  Pagination,
  Select,
  SelectList,
  SelectOption,
  Spinner,
  TextInput,
} from "@patternfly/react-core";
import { Ref, useMemo, useState } from "react";
import { Table, Tbody, Td, Th, Thead, Tr } from "@patternfly/react-table";
import { useTranslation } from "react-i18next";
import {
  type AccountActivity,
  type DayFilter,
  eventTypeFallback,
  eventTypeKey,
} from "../api/accountActivities";
import { formatDateTime } from "../utils/formatDate";
import { isErrorActivity } from "../utils/events";
import styles from "./EventsTable.module.css";

type EventsTableProps = {
  activities: AccountActivity[];
  loading: boolean;
  dayFilter: DayFilter;
  onDayFilter: (filter: DayFilter) => void;
  page: number;
  pageSize: number;
  hasMore: boolean;
  onPage: (page: number) => void;
};

const DAY_FILTERS: { value: DayFilter; labelKey: string }[] = [
  {value: "1", labelKey: "accountActivitiesDateFilterLast1Day"},
  {value: "7", labelKey: "accountActivitiesDateFilterLast7Days"},
  {value: "30", labelKey: "accountActivitiesDateFilterLast30Days"},
  {value: "90", labelKey: "accountActivitiesDateFilterLast90Days"},
];

/** Detail keys rendered as a "Key: value" list. */
const DETAIL_LABELS: Record<string, string> = {
  auth_method: "accountActivitiesDetailAuthMethod",
  identity_provider: "accountActivitiesDetailIdentityProvider",
  identity_provider_auth_method: "accountActivitiesDetailIdpAuthMethod",
  auth_method_details: "accountActivitiesDetailAuthMethodDetails",
};

export const EventsTable = ({
                              activities,
                              loading,
                              dayFilter,
                              onDayFilter,
                              page,
                              pageSize,
                              hasMore,
                              onPage,
                            }: EventsTableProps) => {
  const {t} = useTranslation();
  const [query, setQuery] = useState("");
  const [isFilterOpen, setIsFilterOpen] = useState(false);

  const selectedFilter =
    DAY_FILTERS.find((filter) => filter.value === dayFilter) ?? DAY_FILTERS[2];

  const filteredActivities = useMemo(() => {
    const lowered = query.trim().toLowerCase();

    return activities.filter((activity) => {
      if (!lowered) {
        return true;
      }

      const detailsText = Object.entries(activity.details ?? {})
      .map(([key, value]) => `${key} ${value}`)
      .join(" ");
      return [
        formatDateTime(activity.time),
        activity.type,
        activity.clientId ?? "",
        activity.ipAddress ?? "",
        activity.error ?? "",
        detailsText,
      ]
      .join(" ")
      .toLowerCase()
      .includes(lowered);
    });
  }, [activities, query]);

  const showNoData = activities.length === 0;
  const showNoSearchResults = activities.length > 0 && filteredActivities.length === 0;

  if (showNoData) {
    return (
      <>
        <EmptyState>
          <EmptyStateHeader
            titleText={t("accountActivitiesEmptyTitle")}
          />
          <EmptyStateBody>
            {t("accountActivitiesEmpty")}
          </EmptyStateBody>
        </EmptyState>
      </>
    );
  }

  // The total count is not known client-side, so it is derived from what the server returned:
  // a full page means there may be more after it.
  const itemCount = hasMore
    ? page * pageSize + 1
    : (page - 1) * pageSize + activities.length;

  return (
    <>
      <div className={styles.activityCard}>
        <div className={styles.toolbar}>
          <div className={styles.searchFilter}>
            <TextInput
              type="search"
              value={query}
              onChange={(_event, value) => setQuery(value)}
              aria-label={t("accountActivitiesSearch")}
              placeholder={t("accountActivitiesSearch")}
              className={styles.searchInput}
            />
            <Select
              isOpen={isFilterOpen}
              selected={dayFilter}
              onSelect={(_event, value) => {
                onDayFilter(value as DayFilter);
                setIsFilterOpen(false);
              }}
              onOpenChange={setIsFilterOpen}
              toggle={(toggleRef: Ref<MenuToggleElement>) => (
                <MenuToggle
                  ref={toggleRef}
                  onClick={() => setIsFilterOpen(!isFilterOpen)}
                  isExpanded={isFilterOpen}
                  className={styles.menuToggle}
                >
                  {t(selectedFilter.labelKey)}
                </MenuToggle>
              )}
              className={styles.dayFilter}
            >
              <SelectList>
                {DAY_FILTERS.map((item) => (
                  <SelectOption key={item.value} value={item.value}>
                    {t(item.labelKey)}
                  </SelectOption>
                ))}
              </SelectList>
            </Select>
          </div>
          <p className="pf-v5-u-font-weight-normal pf-v5-u-color-200 pf-v5-u-align-self-end">
            {t("accountActivitiesEdcKeeps90days")}
          </p>
        </div>

        <div className={styles.tableWrap}>
          <Table aria-label={t("accountActivities")} variant="compact" borders
                 className={styles.activityTable}>
            <Thead>
              <Tr>
                <Th style={{verticalAlign: 'middle'}}>{t("accountActivitiesDate")}</Th>
                <Th style={{verticalAlign: 'middle'}}>{t("accountActivitiesEvent")}</Th>
                <Th style={{verticalAlign: 'middle'}}>{t("accountActivitiesClient")}</Th>
                <Th style={{verticalAlign: 'middle'}}>{t("accountActivitiesIpAddress")}</Th>
                <Th style={{verticalAlign: 'middle'}}>{t("accountActivitiesDetails")}</Th>
                <Th style={{verticalAlign: 'middle'}}>{t("accountActivitiesResult")}</Th>
              </Tr>
            </Thead>

            <Tbody>
              {loading ? (
                <Tr>
                  <Td colSpan={6}
                      style={{textAlign: 'center', verticalAlign: 'middle'}}>
                    <div className={styles.tableLoading}>
                      <Spinner className={styles.tableSpinner}/>
                    </div>
                  </Td>
                </Tr>
              ) : (
                filteredActivities.map((activity, index) => {
                  const typeKey = eventTypeKey(activity.type);
                  const details = Object.entries(activity.details ?? {}).filter(
                    ([key]) => key in DETAIL_LABELS,
                  );

                  return (
                    <Tr key={`${activity.time}-${index}`}>
                      <Td style={{verticalAlign: 'middle'}}
                          dataLabel={t("accountActivitiesDate")}>
                        {formatDateTime(activity.time)}
                      </Td>
                      <Td style={{verticalAlign: 'middle'}}
                          dataLabel={t("accountActivitiesEvent")}>
                        <Label
                          color={isErrorActivity(activity) ? "red" : "grey"}
                          isCompact
                        >
                          {t(typeKey, {
                            defaultValue: eventTypeFallback(activity.type),
                          })}
                        </Label>
                        {activity.error ? <div>{activity.error}</div> : null}
                      </Td>
                      <Td style={{verticalAlign: 'middle'}}
                          dataLabel={t("accountActivitiesClient")}>
                        {activity.clientId ?? "-"}
                      </Td>
                      <Td style={{verticalAlign: 'middle'}}
                          dataLabel={t("accountActivitiesIpAddress")}>
                        {activity.ipAddress ?? "-"}
                      </Td>
                      <Td style={{verticalAlign: 'middle'}}
                          dataLabel={t("accountActivitiesDetails")}>
                        {details.length === 0 ? (
                          "-"
                        ) : (
                          <dl>
                            {details.map(([key, value]) => (
                              <div key={key}>
                                <dt>{t(DETAIL_LABELS[key])}</dt>
                                <dd>{value}</dd>
                              </div>
                            ))}
                          </dl>
                        )}
                      </Td>
                      <Td style={{verticalAlign: 'middle'}}
                          dataLabel={t("accountActivitiesResult")}>
                        <Label color={activity.error == null ? "green" : "red"} className="pf-v5-u-font-weight-bold">
                          {activity.error == null ? t("accountActivitiesSuccess") : t("accountActivitiesBlocked")}
                        </Label>
                      </Td>
                    </Tr>
                  );
                })
              )}
            </Tbody>
          </Table>

          {showNoSearchResults && (
            <EmptyState variant={EmptyStateVariant.lg} className={styles.emptyState}>
              <EmptyStateHeader titleText={t("accountActivitiesNoSearchResultsTitle")}/>
              <EmptyStateBody>{t("accountActivitiesNoSearchResults")}</EmptyStateBody>
            </EmptyState>
          )}
        </div>

        <div className={styles.footer}>
          <Pagination
            itemCount={itemCount}
            page={page}
            perPage={pageSize}
            widgetId="account-activities-pagination"
            itemsStart={(page - 1) * pageSize + 1}
            itemsEnd={(page - 1) * pageSize + filteredActivities.length}
            onNextClick={(_event, nextPage) => onPage(nextPage)}
            onPreviousClick={(_event, previousPage) => onPage(previousPage)}
            onSetPage={(_event, nextPage) => onPage(nextPage)}
          />
        </div>
      </div>

      <div className={styles.helpBanner}>
        <p>{t("accountActivity.see-something-you-did-not-recognise")}</p>
      </div>
    </>
  );
};
