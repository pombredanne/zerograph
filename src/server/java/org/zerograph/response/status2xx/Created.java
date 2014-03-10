package org.zerograph.response.status2xx;

public class Created extends Abstract2xx {

    public Created(Object... data) {
        super(data);
    }

    @Override
    public int getStatus() {
        return CREATED;
    }

}
