import type { AccountActivity } from "../api/accountActivities.ts";

/**
 * Event types that represent a failure. Kept in sync with the `*_ERROR` naming used by the
 * server, and matched case-insensitively so a new server-side error type is classified without
 * a frontend change.
 */
export function isErrorActivity(activity: AccountActivity) {
  return activity.type.toUpperCase().endsWith("_ERROR");
}
