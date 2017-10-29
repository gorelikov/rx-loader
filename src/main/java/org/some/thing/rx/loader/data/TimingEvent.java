package org.some.thing.rx.loader.data;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@Data
public class TimingEvent {
  private Integer id;
  private  Long time;
  private Throwable exception;
  @Builder.Default
  private boolean failed = false;
  @Builder.Default
  private boolean initial = false;
  @Builder.Default
  private boolean finished = false;
}
