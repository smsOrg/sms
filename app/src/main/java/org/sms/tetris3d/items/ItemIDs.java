package org.sms.tetris3d.items;

/**
 * Created by hsh on 2016. 12. 16..
 */

public class ItemIDs {
    public final static long OFFSET_ID = 0;
    public final static long MAX_ID = Long.MAX_VALUE;
    public static class BoomIDs extends ItemIDs{
        public final static long OFFSET_ID = 1000;
        public final static long MAX_ID = BoomIDs.OFFSET_ID+1000-1;
    }
    public static class ResetIDs extends ItemIDs{
        public final static long OFFSET_ID = 2000;
        public final static long MAX_ID = ResetIDs.OFFSET_ID+1000-1;
    }
    public static class SwitchIDs extends ItemIDs{
        public final static long OFFSET_ID = 3000;
        public final static long MAX_ID = SwitchIDs.OFFSET_ID+1000-1;
    }
    public static class EtcIDs extends ItemIDs{
        public final static long OFFSET_ID = 900000;
        public final static long MAX_ID = EtcIDs.OFFSET_ID+10000-1;
    }
}