package com.opencsv.bean;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.mocks.*;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvException;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class CsvToBeanTest {
   private static final String TEST_STRING = "name,orderNumber,num\n" +
         "kyle,abc123456,123\n" +
         "jimmy,def098765,456 ";

   private static final String TEST_STRING_WITHOUT_MANDATORY_FIELD = "name,orderNumber,num\n" +
           "kyle,abc123456,123\n" +
           "jimmy,def098765,";

   private CSVReader createReader() {
      return createReader(TEST_STRING);
   }

   private CSVReader createReader(String testString) {
      StringReader reader = new StringReader(testString);
      return new CSVReader(reader);
   }

   @Test(expected = RuntimeException.class)
   public void throwRuntimeExceptionWhenExceptionIsThrown() {
      CsvToBean bean = new CsvToBean();
      bean.parse(new ErrorHeaderMappingStrategy(), createReader());
   }

   @Test(expected = RuntimeException.class)
   public void throwRuntimeExceptionLineWhenExceptionIsThrown() {
      CsvToBean bean = new CsvToBean();
      bean.parse(new ErrorLineMappingStrategy(), new StringReader(TEST_STRING), false); // Extra arguments for code coverage
   }

   @Test
   public void parseBeanWithNoAnnotations() {
      HeaderColumnNameMappingStrategy<MockBean> strategy = new HeaderColumnNameMappingStrategy<>();
      strategy.setType(MockBean.class);
      CsvToBean<MockBean> bean = new CsvToBean<>();

      List<MockBean> beanList = bean.parse(strategy, new StringReader(TEST_STRING), null); // Extra arguments for code coverage
      assertEquals(2, beanList.size());
      assertTrue(beanList.contains(createMockBean("kyle", "abc123456", 123)));
      assertTrue(beanList.contains(createMockBean("jimmy", "def098765", 456)));
   }

   private MockBean createMockBean(String name, String orderNumber, int num) {
      MockBean mockBean = new MockBean();
      mockBean.setName(name);
      mockBean.setOrderNumber(orderNumber);
      mockBean.setNum(num);
      return mockBean;
   }

   @Test
   public void bug133ShouldNotThrowNullPointerExceptionWhenProcessingEmptyWithNoAnnotations() {
      HeaderColumnNameMappingStrategy<Bug133Bean> strategy = new HeaderColumnNameMappingStrategy<>();
      strategy.setType(Bug133Bean.class);

      StringReader reader = new StringReader("one;two;three\n" +
              "kyle;;123\n" +
              "jimmy;;456 ");

      CSVParserBuilder parserBuilder = new CSVParserBuilder();
      CSVReaderBuilder readerBuilder = new CSVReaderBuilder(reader);

      CSVParser parser = parserBuilder.withFieldAsNull(CSVReaderNullFieldIndicator.BOTH).withSeparator(';').build();
      CSVReader csvReader = readerBuilder.withCSVParser(parser).build();

      CsvToBean<Bug133Bean> bean = new CsvToBean<>();

      List<Bug133Bean> beanList = bean.parse(strategy, csvReader, null, true); // Extra arguments for code coverage
      assertEquals(2, beanList.size());
   }

   @Test(expected = IllegalStateException.class)
   public void throwIllegalStateWhenParseWithoutArgumentsIsCalled() {
       CsvToBean csvtb = new CsvToBean();
       csvtb.parse();
   }
   
   @Test(expected = IllegalStateException.class)
   public void throwIllegalStateWhenOnlyReaderIsSpecifiedToParseWithoutArguments() {
       CsvToBean csvtb = new CsvToBean();
       csvtb.setCsvReader(new CSVReader(new StringReader(TEST_STRING)));
       csvtb.parse();
   }
   
   @Test(expected = IllegalStateException.class)
   public void throwIllegalStateWhenOnlyMapperIsSpecifiedToParseWithoutArguments() {
       CsvToBean csvtb = new CsvToBean();
       HeaderColumnNameMappingStrategy<AnnotatedMockBeanFull> strat = new HeaderColumnNameMappingStrategy<>();
       strat.setType(AnnotatedMockBeanFull.class);
       csvtb.setMappingStrategy(strat);
       csvtb.parse();
   }
   
   @Test(expected = IllegalArgumentException.class)
   public void throwIllegalStateWhenReaderNotProvidedInBuilder() {
       new CsvToBeanBuilder<>(null)
               .withType(AnnotatedMockBeanFull.class)
               .build();
   }
   
   @Test(expected = IllegalStateException.class)
   public void throwIllegalStateWhenTypeAndMapperNotProvidedInBuilder() {
       new CsvToBeanBuilder<>(new StringReader(TEST_STRING_WITHOUT_MANDATORY_FIELD))
               .build();
   }
   
   @Test
   public void testMinimumBuilder() {
       List<MinimalCsvBindByPositionBeanForWriting> result =
               new CsvToBeanBuilder<MinimalCsvBindByPositionBeanForWriting>(new StringReader("1,2,3\n4,5,6"))
                       .withType(MinimalCsvBindByPositionBeanForWriting.class)
                       .build()
                       .parse();
       assertEquals(2, result.size());
   }
   
   private class BegToBeFiltered implements CsvToBeanFilter {

      @Override
      public boolean allowLine(String[] line) {
         for(String col : line) {
             if(col.equals("filtermebaby")) return false;
         }
         return true;
      }

   }

   @Test
   public void testMaximumBuilder() throws FileNotFoundException {
       HeaderColumnNameMappingStrategy<AnnotatedMockBeanFull> map = new HeaderColumnNameMappingStrategy<>();
       map.setType(AnnotatedMockBeanFull.class);
       
       // Yeah, some of these are the default values, but I'm having trouble concocting
       // a CSV file screwy enough to meet the requirements posed by not using
       // defaults for everything.
       CsvToBean csvtb =
               new CsvToBeanBuilder<AnnotatedMockBeanFull>(new FileReader("src/test/resources/testinputmaximumbuilder.csv"))
                       .withEscapeChar('?')
                       .withFieldAsNull(CSVReaderNullFieldIndicator.NEITHER) //default
                       .withFilter(new BegToBeFiltered())
                       .withIgnoreLeadingWhiteSpace(false)
                       .withIgnoreQuotations(true)
                       .withKeepCarriageReturn(false) //default
                       .withMappingStrategy(map)
                       .withQuoteChar('!')
                       .withSeparator('#')
                       .withSkipLines(1)
                       .withStrictQuotes(false) // default
                       .withThrowExceptions(false)
                       .withType(AnnotatedMockBeanFull.class)
                       .withVerifyReader(false)
                       .withMultilineLimit(Integer.MAX_VALUE)
                       .build();
       List<CsvException> capturedExceptions = csvtb.getCapturedExceptions();
       assertNotNull(capturedExceptions);
       assertEquals(0, capturedExceptions.size());
       List<AnnotatedMockBeanFull> result = csvtb.parse();
       
       // Three lines, one filtered, one throws an exception
       assertEquals(1, result.size());
       assertEquals(1, csvtb.getCapturedExceptions().size());
       AnnotatedMockBeanFull bean = result.get(0);
       assertEquals("\ttest string of everything!", bean.getStringClass());
       assertTrue(bean.getBoolWrapped());
       assertFalse(bean.isBoolPrimitive());
       assertTrue(bean.getByteWrappedDefaultLocale() == 1);
       // Nothing else really matters
   }
   
   @Test
   public void testColumnMappingStrategyWithBuilder() throws FileNotFoundException {
       List<AnnotatedMockBeanFull> result =
               new CsvToBeanBuilder<AnnotatedMockBeanFull>(new FileReader("src/test/resources/testinputposfullgood.csv"))
                       .withSeparator(';')
                       .withType(AnnotatedMockBeanFull.class)
                       .build()
                       .parse();
       assertEquals(2, result.size());
   }
   
   @Test
   public void testMappingWithoutAnnotationsWithBuilder() {
       List<MockBean> result =
               new CsvToBeanBuilder<MockBean>(new StringReader(TEST_STRING))
                       .withType(MockBean.class)
                       .build()
                       .parse();
       assertEquals(2, result.size());
   }
}
