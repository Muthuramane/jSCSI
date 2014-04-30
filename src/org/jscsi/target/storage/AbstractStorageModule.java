package org.jscsi.target.storage;

import java.io.IOException;

import org.jscsi.target.scsi.cdb.CommandDescriptorBlock;

/**
 * This is an abstract super class offering methods for storage and retrieval of
 * data, as well as emulating some properties of block storage devices.
 * <p>
 * All index and length parameters used by the read and write methods are
 * referring to bytes, unlike the values sent in {@link CommandDescriptorBlock}
 * s, which are based on the value reported by {@link #getBlockSizeInBytes()}.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class AbstractStorageModule {

    /**
     * A fictitious block size.
     */
    protected static final int VIRTUAL_BLOCK_SIZE = 512;

    /**
     * The size of the medium in blocks.
     * 
     * @see #VIRTUAL_BLOCK_SIZE
     */
    protected long sizeInBlocks;

    /**
     * The abstract constructor that makes sure that the {@link #sizeInBlocks}
     * variable is initialized.
     * 
     * @param sizeInBlocks
     *            the size of the medium in blocks
     */
    protected AbstractStorageModule(final long sizeInBlocks) {
        this.sizeInBlocks = sizeInBlocks;
    }

    /**
     * Returns the virtual storage block size in bytes.
     * 
     * @return the virtual storage block size in bytes
     */
    public int getBlockSizeInBytes() {
        return VIRTUAL_BLOCK_SIZE;
    }

    /**
     * Returns the storage space size in bytes divided by
     * {@link #getBlockSizeInBytes()} (rounded down).
     * 
     * @return the virtual amount of storage blocks available
     */
    public long getSizeInBlocks() {
        return sizeInBlocks;
    }

    /**
     * Copies bytes from storage to the passed byte array.
     * 
     * @param bytes
     *            the array into which the data will be copied
     * @param bytesOffset
     *            the position of the first byte in <code>bytes</code>, which
     *            will be filled with data from storage
     * @param length
     *            the number of bytes to copy
     * @param storageIndex
     *            the position of the first byte to be copied
     * @throws IOException
     */
    public abstract void read(byte[] bytes, int bytesOffset, int length,
            long storageIndex) throws IOException;

    /**
     * Copies bytes from storage to the passed byte array.
     * <code>bytes.length</code> bytes will be copied.
     * 
     * @param bytes
     *            the array into which the data will be copied
     * @param storageIndex
     *            he position of the first byte to be copied
     */
    public final void read(byte[] bytes, long storageIndex) throws IOException {
        read(bytes, 0, bytes.length, storageIndex);
    }

    /**
     * Saves part of the passed byte array's content.
     * 
     * @param bytes
     *            the source of the data to be stored
     * @param bytesOffset
     *            offset of the first byte to be stored
     * @param length
     *            the number of bytes to be copied
     * @param storageIndex
     *            byte offset in the storage area
     * @throws IOException
     */
    public abstract void write(byte[] bytes, int bytesOffset, int length,
            long storageIndex) throws IOException;

    /**
     * Saves the whole content of the passed byte array.
     * 
     * @param bytes
     *            the source of the data to be stored
     * @param storageIndex
     *            byte offset in the storage area
     * @throws IOException
     */
    public final void write(byte[] bytes, long storageIndex) throws IOException {
        write(bytes, 0, bytes.length, storageIndex);
    }

    /**
     * This method returns a human-friendly {@link String} representation of the
     * medium's size.
     * <p>
     * Subclasses that do not enforce <code>MEDIUM SIZE %
     * {@link #VIRTUAL_BLOCK_SIZE} == 0</code> should overwrite this method to
     * report unused bytes, if any.
     * 
     * @return a human-friendly representation of the medium's size
     */
    public String getHumanFriendlyMediumSize() {// intentionally not final
        return toHumanFriendlySize(sizeInBlocks * VIRTUAL_BLOCK_SIZE);
    }

    /**
     * This method can be used for checking if a (series of) I/O operations will
     * result in an {@link IOException} due to trying to access blocks outside
     * the medium's boundaries.
     * <p>
     * The SCSI standard requires checking for these boundary violations right
     * after receiving a read or write command, so that an appropriate error
     * message can be returned to the initiator. Therefore this method must be
     * called prior to each read or write sequence.
     * <p>
     * The values returned by this method and their meaning with regard to the
     * interval [0, {@link #sizeInBlocks} - 1] are shown in the following table:
     * <p>
     * <table border="1">
     * <tr>
     * <th>Return Value</th>
     * <th>Meaning</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>no boundaries are violated</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>the <i>logicalBlockAddress</i> parameter lies outside of the interval
     * </td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>the interval [<i>logicalBlockAddress</i>, <i>logicalBlockAddress</i>
     * + <i>transferLengthInBlocks</i>]<br/>
     * lies outside of the interval, or <i>transferLengthInBlocks</i> is
     * negative</td>
     * </tr>
     * </table>
     * <p>
     * Note that the parameters of this method are referring to blocks, not to
     * byte indices.
     * 
     * @param logicalBlockAddress
     *            the index of the first block of data to be read or written
     * @param transferLengthInBlocks
     *            the total number of consecutive blocks about to be read or
     *            written
     * @return see table in description
     */
    public final int checkBounds(final long logicalBlockAddress,
            final int transferLengthInBlocks) {
        if (logicalBlockAddress < 0 || logicalBlockAddress >= sizeInBlocks)
            return 1;
        if (transferLengthInBlocks < 0
                || logicalBlockAddress + transferLengthInBlocks > sizeInBlocks)
            return 2;
        return 0;
    }

    /**
     * Returns a more human-friendly representation of a value in "bytes",
     * breaking down the passed value into larger byte units.
     * 
     * @param sizeInBytes
     *            a value specifying a certain number of bytes
     * @return a more human-friendly representation of the passed value
     */
    protected static final String toHumanFriendlySize(final long sizeInBytes) {

        // some stuff we will need
        final long factor = 1024;
        final String[] byteUnits = { "B", "KiB", "MiB", "GiB", "TiB" };
        final StringBuilder sb = new StringBuilder();

        // begin building output
        sb.append(sizeInBytes);
        sb.append(" bytes (");

        if (sizeInBytes <= 0) {// early exit
            sb.append("nothing)");
            return sb.toString();
        }

        // calculate the size in different units (non-inclusive)
        long size = sizeInBytes;
        final long[] values = new long[byteUnits.length];
        for (int i = 0; i < values.length - 1; ++i) {
            values[i] = size % factor;
            size /= factor;
        }
        values[values.length - 1] = size;// the value for the biggest unit may
                                         // be >= factor

        // append the non-zero values + units
        boolean addSpace = false;
        for (int i = values.length - 1; i >= 0; --i) {
            if (values[i] > 0) {
                if (addSpace)
                    sb.append(" ");
                sb.append(values[i]);
                sb.append(byteUnits[i]);
                addSpace = true;
            }
        }
        sb.append(")");

        // report unused bytes
        if (values[0] % VIRTUAL_BLOCK_SIZE != 0) {
            sb.append(", ");
            sb.append(values[0] % VIRTUAL_BLOCK_SIZE);
            if (values[0] == 1)
                sb.append(" byte is not used");
            else
                sb.append(" bytes are not used");
        }
        return sb.toString();
    }

}
