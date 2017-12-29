package com.opencsv;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Andre Rosot
 */
public class CsvReaderHeaderAwareTest {

    private CSVReaderHeaderAware csvr;

    @Before
    public void setUpWithHeader() throws Exception {
        StringBuilder sb = new StringBuilder(ICSVParser.INITIAL_READ_SIZE);
        sb.append("first,second,third\n");
        sb.append("a,b,c").append("\n");   // standard case
        sb.append("a,\"b,b,b\",c").append("\n");  // quoted elements
        sb.append(",,").append("\n"); // empty elements
        sb.append("a,\"PO Box 123,\nKippax,ACT. 2615.\nAustralia\",d.\n");
        sb.append("\"Glen \"\"The Man\"\" Smith\",Athlete,Developer\n"); // Test quoted quote chars
        sb.append("\"\"\"\"\"\",\"test\"\n"); // """""","test"  representing:  "", test
        sb.append("\"a\nb\",b,\"\nd\",e\n");
        sb.append("a");
        csvr = new CSVReaderHeaderAware(new StringReader(sb.toString()));
    }

    @Test
    public void shouldKeepBasicParsing() throws IOException {
        String[] nextLine = csvr.readNext();
        assertEquals("a", nextLine[0]);
        assertEquals("b", nextLine[1]);
        assertEquals("c", nextLine[2]);

        // test quoted commas
        nextLine = csvr.readNext();
        assertEquals("a", nextLine[0]);
        assertEquals("b,b,b", nextLine[1]);
        assertEquals("c", nextLine[2]);

        // test empty elements
        nextLine = csvr.readNext();
        assertEquals(3, nextLine.length);

        // test multiline quoted
        nextLine = csvr.readNext();
        assertEquals(3, nextLine.length);

        // test quoted quote chars
        nextLine = csvr.readNext();
        assertEquals("Glen \"The Man\" Smith", nextLine[0]);

        nextLine = csvr.readNext();
        assertEquals("\"\"", nextLine[0]); // check the tricky situation
        assertEquals("test", nextLine[1]); // make sure we didn't ruin the next field..

        nextLine = csvr.readNext();
        assertEquals(4, nextLine.length);

        assertEquals("a", csvr.readNext()[0]);

        //test end of stream
        assertNull(csvr.readNext());
    }

    @Test
    public void shouldRetrieveColumnsByHeaderName() throws IOException {
        assertEquals("a", csvr.readNext("first"));
        assertEquals("a", csvr.readNext("first"));
        assertEquals("", csvr.readNext("first"));
        assertEquals("PO Box 123,\nKippax,ACT. 2615.\nAustralia", csvr.readNext("second"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForInvalidColumn() throws IOException {
        csvr.readNext("fourth");
    }

    @Test(expected = IOException.class)
    public void shouldFailWhenNumberOfDataItemsIsLessThanHeader() throws IOException {
        csvr.skip(7);
        csvr.readNext("second");
    }

    @Test(expected = IOException.class)
    public void shouldFailWhenNumberOfDataItemsIsGreaterThanHeader() throws IOException {
        csvr.skip(6);
        csvr.readNext("second");
    }

    @Test
    public void shouldRetrieveMap() throws IOException {
        Map<String, String> mappedLine = csvr.readMap();
        assertEquals("a", mappedLine.get("first"));
        assertEquals("b", mappedLine.get("second"));
        assertEquals("c", mappedLine.get("third"));

        csvr.skip(2);

        mappedLine = csvr.readMap();
        assertEquals("a", mappedLine.get("first"));
        assertEquals("PO Box 123,\nKippax,ACT. 2615.\nAustralia", mappedLine.get("second"));
        assertEquals("d.", mappedLine.get("third"));
    }

    @Test(expected = IOException.class)
    public void readMapThrowsExceptionIfNumberOfDataItemsIsGreaterThanHeader() throws IOException {
        Map<String, String> mappedLine = csvr.readMap();
        assertEquals("a", mappedLine.get("first"));
        assertEquals("b", mappedLine.get("second"));
        assertEquals("c", mappedLine.get("third"));

        csvr.skip(5);

        mappedLine = csvr.readMap();
    }

    @Test(expected = IOException.class)
    public void readMapThrowsExceptionIfNumberOfDataItemsIsLessThanHeader() throws IOException {
        Map<String, String> mappedLine = csvr.readMap();
        assertEquals("a", mappedLine.get("first"));
        assertEquals("b", mappedLine.get("second"));
        assertEquals("c", mappedLine.get("third"));

        csvr.skip(6);

        mappedLine = csvr.readMap();
    }

    @Test
    public void shouldReturnNullWhenFileIsOver() throws IOException {
        csvr.skip(8);
        assertNull(csvr.readMap());
    }

    @Test
    public void readNextWhenPastEOF() throws IOException {
        csvr.skip(8);
        assertNull(csvr.readNext("first"));
    }
}
