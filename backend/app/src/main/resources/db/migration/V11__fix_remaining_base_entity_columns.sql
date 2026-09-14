-- V11__fix_remaining_base_entity_columns.sql
--
-- V10 missed a handful of tables when reconciling with BaseEntity's required
-- columns. Verified against a live schema diff (information_schema.columns)
-- rather than static reading, to close every remaining gap in one pass.

-- audit_logs, login_history: real schema has neither deleted_at nor a missing
-- created_at (both tables are created with their exact final real-schema
-- shape directly in V5), so no reconciliation is needed here.
