package org.zerograph.response.status2xx;

public class NoContent extends Status2xx {

    public NoContent() {
        super();
    }

    @Override
    public int getStatus() {
        return NO_CONTENT;
    }

}
