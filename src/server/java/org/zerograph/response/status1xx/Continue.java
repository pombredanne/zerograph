package org.zerograph.response.status1xx;

public class Continue extends Status1xx {

    public Continue(Object... data) {
        super(data);
    }

    @Override
    public int getStatus() {
        return CONTINUE;
    }

}
