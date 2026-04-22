package ships;

public abstract class Ship {

    protected int length;
    protected int row;
    protected int col;
    protected boolean horizontal = true;

    protected int health;

    public Ship(int length) {
        this.length = length;
        this.health = length;
    }

    public int getLength() { return length; }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public boolean isHorizontal() { return horizontal; }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void rotate() {
        horizontal = !horizontal;
    }

    // NEW METHODS
    public void hit() {
        health--;
    }

    public boolean isSunk() {
        return health <= 0;
    }
}