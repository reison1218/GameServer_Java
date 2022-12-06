package game.utils;

/**
 * @author tangjian
 * @date 2022-11-23 14:51
 * desc
 */
public class MD5State {

    int[] state;
    long count;
    byte[] buffer;

    public MD5State() {
        this.buffer = new byte[64];
        this.count = 0L;
        this.state = new int[4];
        this.state[0] = 1732584193;
        this.state[1] = -271733879;
        this.state[2] = -1732584194;
        this.state[3] = 271733878;
    }

    public MD5State(MD5State var1) {
        this();

        int var2;
        for(var2 = 0; var2 < this.buffer.length; ++var2) {
            this.buffer[var2] = var1.buffer[var2];
        }

        for(var2 = 0; var2 < this.state.length; ++var2) {
            this.state[var2] = var1.state[var2];
        }

        this.count = var1.count;
    }
}
