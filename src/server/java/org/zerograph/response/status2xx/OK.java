package org.zerograph.response.status2xx;

public class OK extends Abstract2xx {

    public OK(Object... data) {
        super(data);
    }

    @Override
    public int getStatus() {
        return OK;
    }

}
