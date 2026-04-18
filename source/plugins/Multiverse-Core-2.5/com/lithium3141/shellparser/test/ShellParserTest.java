package com.lithium3141.shellparser.test;

import com.lithium3141.shellparser.ParseException;
import com.lithium3141.shellparser.ShellParser;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;

public class ShellParserTest {
   public ShellParserTest() {
      super();
   }

   @Test
   public void testEmpty() {
      Assert.assertEquals(new ArrayList(), ShellParser.safeParseString(""));
   }

   @Test
   public void testWord() {
      Assert.assertEquals(new ArrayList() {
         {
            this.add("test");
         }
      }, ShellParser.safeParseString("test"));
   }

   @Test
   public void testTwoWords() {
      Assert.assertEquals(new ArrayList() {
         {
            this.add("a");
            this.add("b");
         }
      }, ShellParser.safeParseString("a b"));
   }

   @Test
   public void testManyWords() {
      List<String> expected = new ArrayList() {
         {
            this.add("a");
            this.add("b");
            this.add("c");
            this.add("d");
            this.add("e");
         }
      };
      Assert.assertEquals(expected, ShellParser.safeParseString("a b c d e"));
   }

   @Test
   public void testEscapedLiteral() {
      Assert.assertEquals(new ArrayList() {
         {
            this.add("test");
         }
      }, ShellParser.safeParseString("\\test"));
   }

   @Test
   public void testDoubleQuotes() {
      Assert.assertEquals(new ArrayList() {
         {
            this.add("test test");
         }
      }, ShellParser.safeParseString("\"test test\""));
   }

   @Test
   public void testMixedDoubleQuotes() {
      Assert.assertEquals(new ArrayList() {
         {
            this.add("test");
            this.add("test test");
            this.add("test");
         }
      }, ShellParser.safeParseString("test \"test test\" test"));
   }

   @Test
   public void testSingleQuotes() {
      Assert.assertEquals(new ArrayList() {
         {
            this.add("test test");
         }
      }, ShellParser.safeParseString("'test test'"));
   }

   @Test
   public void testMixedSingleQuotes() {
      Assert.assertEquals(new ArrayList() {
         {
            this.add("test");
            this.add("test test");
            this.add("test");
         }
      }, ShellParser.safeParseString("test 'test test' test"));
   }

   @Test
   public void testMixedQuotes() {
      Assert.assertEquals(new ArrayList() {
         {
            this.add("test test");
            this.add("test test");
         }
      }, ShellParser.safeParseString("\"test test\" 'test test'"));
   }

   @Test
   public void testNestedQuotes() {
      Assert.assertEquals(new ArrayList() {
         {
            this.add("test 'test test'");
         }
      }, ShellParser.safeParseString("\"test 'test test'\""));
   }

   @Test(
      expected = ParseException.class
   )
   public void testMismatchedDoubleQuote() throws ParseException {
      ShellParser.parseString("\"");
   }

   @Test(
      expected = ParseException.class
   )
   public void testMismatchedSingleQuote() throws ParseException {
      ShellParser.parseString("'");
   }

   @Test(
      expected = ParseException.class
   )
   public void testBadEscape() throws ParseException {
      ShellParser.parseString("\\");
   }
}
