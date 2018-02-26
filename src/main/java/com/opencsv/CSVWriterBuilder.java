package com.opencsv;

import java.io.Writer;

/**
 * Builder for creating the CSVWriter.
 * <p>Note: this should be the preferred method of creating the CSVWriter as
 * we will no longer be creating constructors for new fields added to the
 * writer.  Plus there are now multiple flavors of CSVWriter and this will
 * help build the correct one.</p>
 * <br>
 * <p>If a CSVWriterBuilder has a parser injected in it will create a CSVParserWriter, otherwise
 * it will create a CSVWriter.  If a parser is injected into a builder that already has a separator,
 * quotechar, or escapechar then an IllegalArguementException is thrown.  Likewise the opposite
 * is true.</p>
 * <br>
 * <p>If nothing is defined then a CSVWriter will be produced with default settings.</p>
 * <p>
 * <code>
 * Writer writer = new StringWriter();  // any Writer<br>
 * CSVParser parser = new CSVParserBuilder().build();<br>
 * ICSVWriter csvParserWriter = new CSVWriterBuilder(writer)<br>
 * .withParser(parser)<br>
 * .withLineEnd(ICSVWriter.RFC4180_LINE_END)<br>
 * .build();  // will produce a CSVParserWriter<br>
 * <br>
 * ICSVWriter csvWriter = new CSVWriterBuilder(writer)<br>
 * .withSeparator(ICSVParser.DEFAULT_SEPARATOR)<br>
 * .withQuoteChar(ICSVParser.DEFAULT_QUOTE_CHARACTER)<br>
 * .withEscapeChar(ICSVParser.DEFAULT_ESCAPE_CHARACTER)<br>
 * .withLineEnd(ICSVWriter.DEFAULT_LINE_END)<br>
 * .build();  // will produce a CSVWriter<br>
 * </code>
 *
 * @since 4.2
 */
public class CSVWriterBuilder {
    private final Writer writer;
    private ICSVParser parser;
    private Character separator;
    private Character quotechar;
    private Character escapechar;
    private String lineEnd = ICSVWriter.DEFAULT_LINE_END;

    /**
     * Constructor taking a writer for the resulting CSV output.  This is because the Writer is required and
     * everything else has an optional default.
     *
     * @param writer - A writer to create the resulting CSV output for the writer.
     */
    public CSVWriterBuilder(Writer writer) {
        this.writer = writer;
    }


    /**
     * Sets the parser that the ICSVWriter will be using.  If none is defined then a CSVWriter will be returned
     * when the build command is executed.
     *
     * @param parser - parser to inject into the ICSVWriter.
     * @return - The CSVWriterBuilder with the parser set.
     * @throws - IllegalArgumentException if a separator, quote or escape character has been set.
     */
    public CSVWriterBuilder withParser(ICSVParser parser) {
        if (separator != null || quotechar != null || escapechar != null) {
            throw new IllegalArgumentException("You cannot set the parser in the builder if you have set the separator, quote, or escape character");
        }
        this.parser = parser;
        return this;
    }

    /**
     * Sets the separator that the ICSVWriter will be using.
     *
     * @param separator - the separator character to use when creating the CSV content.
     * @return - The CSVWriterBuilder with the separator set.
     * @throws - IllegalArgumentException if a parser has been set.
     */
    public CSVWriterBuilder withSeparator(char separator) {
        if (parser != null) {
            throw new IllegalArgumentException("You cannot set the separator in the builder if you have a ICSVParser set.  Set the separator in the parser instead.");
        }
        this.separator = separator;
        return this;
    }

    /**
     * Sets the quote character that the ICSVWriter will be using.
     *
     * @param quoteChar - the quote character to use when creating the CSV content.
     * @return - The CSVWriterBuilder with the quote character set.
     * @throws - IllegalArgumentException if a parser has been set.
     */
    public CSVWriterBuilder withQuoteChar(char quoteChar) {
        if (parser != null) {
            throw new IllegalArgumentException("You cannot set the quote character in the builder if you have a ICSVParser set.  Set the quote character in the parser instead.");
        }
        this.quotechar = quoteChar;
        return this;
    }

    /**
     * Sets the escape character that the ICSVWriter will be using.
     *
     * @param escapeChar - the quote character to use when creating the CSV content.
     * @return - The CSVWriterBuilder with the escape character set.
     * @throws - IllegalArgumentException if a parser has been set.
     */
    public CSVWriterBuilder withEscapeChar(char escapeChar) {
        if (parser != null) {
            throw new IllegalArgumentException("You cannot set the escape character in the builder if you have a ICSVParser set.  Set the escape character in the parser instead.");
        }
        this.escapechar = escapeChar;
        return this;
    }

    /**
     * Sets the newLine character that the ICSVWriter will use.  If none is defined then a "\n" will be used
     *
     * @param lineEnd - newline string to inject into the ICSVWriter.
     * @return - The CSVWriterBuilder with the lineEnd set.
     */
    public CSVWriterBuilder withLineEnd(String lineEnd) {
        this.lineEnd = lineEnd;
        return this;
    }
}