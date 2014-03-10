package org.zerograph.response.status1xx;

public class Continue extends Abstract1xx {

    public Continue(Object... data) {
        super(data);
    }

    @Override
    public int getStatus() {
        return CONTINUE;
    }

}
