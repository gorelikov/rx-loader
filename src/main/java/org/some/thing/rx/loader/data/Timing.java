package org.some.thing.rx.loader.data;

import lombok.*;
import lombok.experimental.Wither;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Objects;

@ToString
@NoArgsConstructor
@Wither
@AllArgsConstructor
public class Timing {
  private Integer id;
  private Long initialTime;
  private List<Long> times;
  @Getter
  private boolean finished = false;
  @Getter
  private boolean failed = false;
  @Getter
  private boolean measured = false;
  private LongSummaryStatistics cachedStatistics;

  public void accept(TimingEvent event) {
    if (times == null)
      this.times = new ArrayList<>();
    this.id = event.getId();
    if (event.isInitial())
      this.initialTime = event.getTime();
    else {
      this.measured = true;
      this.finished |= event.isFinished();
      this.failed |= event.isFailed();
      this.times.add(event.getTime());
    }
  }

  public Timing combine(Timing other) {
    Timing result = this.withFinished(this.finished | other.finished);
    result.finished = this.finished |  other.finished;
    result.times.addAll(other.times);
    return result;
  }

  public LongSummaryStatistics getTimings() {
    if (cachedStatistics != null)
      return cachedStatistics;
    return cachedStatistics = times.stream().filter(Objects::nonNull).mapToLong(res -> res - initialTime).summaryStatistics();
  }


}
