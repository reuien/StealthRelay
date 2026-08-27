package exceptions;

import java.util.Date;

/**
 * Exception that occurs when some process tried to write to the wrong chunk.
 */
public class WrongChunkException extends WriteException {

    public WrongChunkException(Date valueTimestamp, Date chunkStartTimestamp, Date chunkEndTimestamp, long value) {
        super("ERROR: 向 " + chunkStartTimestamp + " / " +
                        chunkStartTimestamp.getTime() + " 到 " + chunkEndTimestamp + " / " +
                        chunkEndTimestamp.getTime() + "的块中写入 " + valueTimestamp.toString() +
                        " / " + valueTimestamp.getTime() + " 时刻的数据 " + value);

    }

    public WrongChunkException(String msg) {
        super(msg);

    }
}
