CREATE TABLE trade_audit (
    id BIGSERIAL PRIMARY KEY,
    trade_id UUID NOT NULL,
    operation VARCHAR(10) NOT NULL CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100) NOT NULL DEFAULT 'system',
    old_data JSONB,
    new_data JSONB
);

CREATE INDEX idx_trade_audit_trade_id_changed_at
    ON trade_audit(trade_id, changed_at DESC);

CREATE OR REPLACE FUNCTION record_trade_audit()
RETURNS TRIGGER AS $$
DECLARE
    actor VARCHAR(100);
BEGIN
    actor := COALESCE(
        NULLIF(current_setting('app.current_user', TRUE), ''),
        CASE WHEN TG_OP = 'DELETE' THEN OLD.updated_by ELSE NEW.updated_by END,
        'system'
    );

    IF TG_OP = 'INSERT' THEN
        INSERT INTO trade_audit (trade_id, operation, changed_by, new_data)
        VALUES (NEW.id, TG_OP, actor, to_jsonb(NEW));
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO trade_audit (trade_id, operation, changed_by, old_data, new_data)
        VALUES (NEW.id, TG_OP, actor, to_jsonb(OLD), to_jsonb(NEW));
        RETURN NEW;
    ELSE
        INSERT INTO trade_audit (trade_id, operation, changed_by, old_data)
        VALUES (OLD.id, TG_OP, actor, to_jsonb(OLD));
        RETURN OLD;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_trades_audit
AFTER INSERT OR UPDATE OR DELETE ON trades
FOR EACH ROW EXECUTE FUNCTION record_trade_audit();
