package bicinetica.com.bicinetica.data;

import java.util.UUID;

public class ServiceData {
    private UUID uuid;
    private ServiceType type;

    public ServiceData() {
        uuid = null;
        type = ServiceType.Unknown;

    }

    public ServiceData(UUID uuid, ServiceType type) {
        this.uuid = uuid;
        this.type = type;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    public enum ServiceType {
        Unknown,
        CyclingSpeedAndCadence,
        CyclingPower
    }
}
