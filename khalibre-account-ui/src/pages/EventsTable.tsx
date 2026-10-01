import {
  EmptyState,
  EmptyStateBody,
  EmptyStateHeader,
  Label,
  Pagination,
  Spinner,
} from "@patternfly/react-core";
import { Table, Tbody, Td, Th, Thead, Tr } from "@patternfly/react-table";
import { useTranslation } from "react-i18next";
import {
  eventTypeFallback,
  eventTypeKey,
  type AccountActivity,
} from "../api/accountActivities";
import { formatDateTime } from "../utils/formatDate";
import { isErrorActivity } from "../utils/events";

type EventsTableProps = {
  activities: AccountActivity[];
  loading: boolean;
  page: number;
  pageSize: number;
  hasMore: boolean;
  onPage: (page: number) => void;
  emptyLabel: string;
};

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
  page,
  pageSize,
  hasMore,
  onPage,
  emptyLabel,
}: EventsTableProps) => {
  const { t } = useTranslation();

  if (loading) {
    return <Spinner />;
  }

  if (activities.length === 0) {
    return (
      <EmptyState>
        <EmptyStateHeader titleText={t("accountActivitiesEmptyTitle")} />
        <EmptyStateBody>{emptyLabel}</EmptyStateBody>
      </EmptyState>
    );
  }

  // The total count is not known client-side, so it is derived from what the server returned:
  // a full page means there may be more after it.
  const itemCount = hasMore
    ? page * pageSize + 1
    : (page - 1) * pageSize + activities.length;

  return (
    <>
      <div className="pf-v5-c-table-container">
        <Table aria-label={t("accountActivities")} variant="compact">
          <Thead>
            <Tr>
              <Th>{t("accountActivitiesDate")}</Th>
              <Th>{t("accountActivitiesEvent")}</Th>
              <Th>{t("accountActivitiesClient")}</Th>
              <Th>{t("accountActivitiesIpAddress")}</Th>
              <Th>{t("accountActivitiesDetails")}</Th>
            </Tr>
          </Thead>
          <Tbody>
            {activities.map((activity, index) => {
              const typeKey = eventTypeKey(activity.type);
              const details = Object.entries(activity.details ?? {}).filter(
                ([key]) => key in DETAIL_LABELS,
              );

              return (
                <Tr key={`${activity.time}-${index}`}>
                  <Td dataLabel={t("accountActivitiesDate")}>
                    {formatDateTime(activity.time)}
                  </Td>
                  <Td dataLabel={t("accountActivitiesEvent")}>
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
                  <Td dataLabel={t("accountActivitiesClient")}>
                    {activity.clientId ?? "-"}
                  </Td>
                  <Td dataLabel={t("accountActivitiesIpAddress")}>
                    {activity.ipAddress ?? "-"}
                  </Td>
                  <Td dataLabel={t("accountActivitiesDetails")}>
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
                </Tr>
              );
            })}
          </Tbody>
        </Table>
      </div>

      <Pagination
        itemCount={itemCount}
        page={page}
        perPage={pageSize}
        widgetId="account-activities-pagination"
        itemsStart={(page - 1) * pageSize + 1}
        itemsEnd={(page - 1) * pageSize + activities.length}
        onNextClick={(_event, nextPage) => onPage(nextPage)}
        onPreviousClick={(_event, previousPage) => onPage(previousPage)}
        onSetPage={(_event, nextPage) => onPage(nextPage)}
      />
    </>
  );
};
