package utility;

public class CellCoord {
    private final int row;
    private final int col;
    private final String identity;

    public CellCoord(int row, int col, String identity) {
        this.row = row;
        this.col = col;
        this.identity = identity;
    }
    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String getIdentity() {
        return identity;
    }
}
