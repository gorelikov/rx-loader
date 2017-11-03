package org.some.thing.rx.loader.data;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

@Builder
@Data
public class TimingEvent {
    private Integer id;
    private Long time;
    private Throwable exception;
    @Default
    private boolean failed = false;
    @Default
    private boolean initial = false;
    @Default
    private boolean finished = false;
}
