package bicinetica.com.bicinetica.model.bluetooth.characteristics;

import java.util.UUID;

public abstract class Characteristic {
    private UUID mUuid;


    protected Characteristic(UUID uuid) {
        mUuid = uuid;
    }

    public UUID getUuid() {
        return mUuid;
    }
    //protected abstract int decode(int offset);
}
