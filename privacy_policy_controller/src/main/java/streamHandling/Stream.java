package streamHandling;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Stream {
    private final String name;
    private final String description;
    private final long id;
    private final Date startDate;
    private Date endDate;
    private final long chunkSize;
    private final TimeUtil.Precision precision;
    private final List<TimeUtil.Precision> resolutionLevels;


    public Stream(long id, String name, String description, Date streamStartDate, TimeUtil.Precision precision,
                  List<TimeUtil.Precision> resolutionLevels) throws IOException {
        this.name = name;
        this.description = description;
        this.id = id;
        this.startDate = streamStartDate;
        this.chunkSize = precision.getMillis();
        this.precision = precision;
        this.resolutionLevels = resolutionLevels;
    }

    public Stream(long id, String name, String description, Date streamStartDate, Date streamEndDate,
                  TimeUtil.Precision precision, List<TimeUtil.Precision> resolutionLevels) throws IOException {
        this.name = name;
        this.description = description;
        this.id = id;
        this.startDate = streamStartDate;
        this.endDate = streamEndDate;
        this.chunkSize = precision.getMillis();
        this.precision = precision;
        this.resolutionLevels = resolutionLevels;
    }

    public Stream(long id, String name, String description, Date streamStartDate, Date streamEndDate,
                  long chunkSize, long resolutionLevel) throws IOException {
        this.name = name;
        this.description = description;
        this.id = id;
        this.startDate = streamStartDate;
        this.endDate = streamEndDate;
        this.chunkSize = chunkSize;
        this.precision = serchPrecision(chunkSize);
        this.resolutionLevels = Collections.singletonList(serchPrecision(resolutionLevel));
    }

    public Stream(long id, String name, String description, TimeUtil.Precision precision, List<TimeUtil.Precision> resolutionLevels) throws IOException {
        this(id, name, description, TimeUtil.getDateAtLastFullMinute(), precision, resolutionLevels);
    }

    public TimeUtil.Precision serchPrecision(long chunkSize){
        for (TimeUtil.Precision prc : TimeUtil.Precision.values()){
            if (prc.getMillis() == chunkSize){
                return prc;
            }
        }
        return null;
    }


    public TimeUtil.Precision getPrecision() {
        return precision;
    }

    public long getChunkSize() {
        return chunkSize;
    }
    public List<TimeUtil.Precision> getResolutionLevels() {
        return resolutionLevels;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setEndDate(Date endDate){
        this.endDate = endDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }


}
