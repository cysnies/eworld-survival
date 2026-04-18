package javax.mail.internet;

import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.LineOutputStream;
import com.sun.mail.util.PropUtil;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessageAware;
import javax.mail.MessageContext;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.MultipartDataSource;

public class MimeMultipart extends Multipart {
   protected DataSource ds;
   protected boolean parsed;
   protected boolean complete;
   protected String preamble;
   protected boolean ignoreMissingEndBoundary;
   protected boolean ignoreMissingBoundaryParameter;
   protected boolean ignoreExistingBoundaryParameter;
   protected boolean allowEmpty;

   public MimeMultipart() {
      this("mixed");
   }

   public MimeMultipart(String subtype) {
      super();
      this.ds = null;
      this.parsed = true;
      this.complete = true;
      this.preamble = null;
      this.ignoreMissingEndBoundary = true;
      this.ignoreMissingBoundaryParameter = true;
      this.ignoreExistingBoundaryParameter = false;
      this.allowEmpty = false;
      String boundary = UniqueValue.getUniqueBoundaryValue();
      ContentType cType = new ContentType("multipart", subtype, (ParameterList)null);
      cType.setParameter("boundary", boundary);
      this.contentType = cType.toString();
      this.initializeProperties();
   }

   public MimeMultipart(BodyPart... parts) throws MessagingException {
      this();

      for(BodyPart bp : parts) {
         super.addBodyPart(bp);
      }

   }

   public MimeMultipart(String subtype, BodyPart... parts) throws MessagingException {
      this(subtype);

      for(BodyPart bp : parts) {
         super.addBodyPart(bp);
      }

   }

   public MimeMultipart(DataSource ds) throws MessagingException {
      super();
      this.ds = null;
      this.parsed = true;
      this.complete = true;
      this.preamble = null;
      this.ignoreMissingEndBoundary = true;
      this.ignoreMissingBoundaryParameter = true;
      this.ignoreExistingBoundaryParameter = false;
      this.allowEmpty = false;
      if (ds instanceof MessageAware) {
         MessageContext mc = ((MessageAware)ds).getMessageContext();
         this.setParent(mc.getPart());
      }

      if (ds instanceof MultipartDataSource) {
         this.setMultipartDataSource((MultipartDataSource)ds);
      } else {
         this.parsed = false;
         this.ds = ds;
         this.contentType = ds.getContentType();
      }
   }

   protected void initializeProperties() {
      this.ignoreMissingEndBoundary = PropUtil.getBooleanSystemProperty("mail.mime.multipart.ignoremissingendboundary", true);
      this.ignoreMissingBoundaryParameter = PropUtil.getBooleanSystemProperty("mail.mime.multipart.ignoremissingboundaryparameter", true);
      this.ignoreExistingBoundaryParameter = PropUtil.getBooleanSystemProperty("mail.mime.multipart.ignoreexistingboundaryparameter", false);
      this.allowEmpty = PropUtil.getBooleanSystemProperty("mail.mime.multipart.allowempty", false);
   }

   public synchronized void setSubType(String subtype) throws MessagingException {
      ContentType cType = new ContentType(this.contentType);
      cType.setSubType(subtype);
      this.contentType = cType.toString();
   }

   public synchronized int getCount() throws MessagingException {
      this.parse();
      return super.getCount();
   }

   public synchronized BodyPart getBodyPart(int index) throws MessagingException {
      this.parse();
      return super.getBodyPart(index);
   }

   public synchronized BodyPart getBodyPart(String CID) throws MessagingException {
      this.parse();
      int count = this.getCount();

      for(int i = 0; i < count; ++i) {
         MimeBodyPart part = (MimeBodyPart)this.getBodyPart(i);
         String s = part.getContentID();
         if (s != null && s.equals(CID)) {
            return part;
         }
      }

      return null;
   }

   public boolean removeBodyPart(BodyPart part) throws MessagingException {
      this.parse();
      return super.removeBodyPart(part);
   }

   public void removeBodyPart(int index) throws MessagingException {
      this.parse();
      super.removeBodyPart(index);
   }

   public synchronized void addBodyPart(BodyPart part) throws MessagingException {
      this.parse();
      super.addBodyPart(part);
   }

   public synchronized void addBodyPart(BodyPart part, int index) throws MessagingException {
      this.parse();
      super.addBodyPart(part, index);
   }

   public synchronized boolean isComplete() throws MessagingException {
      this.parse();
      return this.complete;
   }

   public synchronized String getPreamble() throws MessagingException {
      this.parse();
      return this.preamble;
   }

   public synchronized void setPreamble(String preamble) throws MessagingException {
      this.preamble = preamble;
   }

   protected synchronized void updateHeaders() throws MessagingException {
      this.parse();

      for(int i = 0; i < this.parts.size(); ++i) {
         ((MimeBodyPart)this.parts.elementAt(i)).updateHeaders();
      }

   }

   public synchronized void writeTo(OutputStream os) throws IOException, MessagingException {
      this.parse();
      String boundary = "--" + (new ContentType(this.contentType)).getParameter("boundary");
      LineOutputStream los = new LineOutputStream(os);
      if (this.preamble != null) {
         byte[] pb = ASCIIUtility.getBytes(this.preamble);
         los.write(pb);
         if (pb.length > 0 && pb[pb.length - 1] != 13 && pb[pb.length - 1] != 10) {
            los.writeln();
         }
      }

      if (this.parts.size() == 0) {
         if (!this.allowEmpty) {
            throw new MessagingException("Empty multipart: " + this.contentType);
         }

         los.writeln(boundary);
         los.writeln();
      } else {
         for(int i = 0; i < this.parts.size(); ++i) {
            los.writeln(boundary);
            ((MimeBodyPart)this.parts.elementAt(i)).writeTo(os);
            los.writeln();
         }
      }

      los.writeln(boundary + "--");
   }

   protected synchronized void parse() throws MessagingException {
      if (!this.parsed) {
         this.initializeProperties();
         InputStream in = null;
         SharedInputStream sin = null;
         long start = 0L;
         long end = 0L;

         try {
            in = this.ds.getInputStream();
            if (!(in instanceof ByteArrayInputStream) && !(in instanceof BufferedInputStream) && !(in instanceof SharedInputStream)) {
               in = new BufferedInputStream(in);
            }
         } catch (Exception ex) {
            throw new MessagingException("No inputstream from datasource", ex);
         }

         if (in instanceof SharedInputStream) {
            sin = (SharedInputStream)in;
         }

         ContentType cType = new ContentType(this.contentType);
         String boundary = null;
         if (!this.ignoreExistingBoundaryParameter) {
            String bp = cType.getParameter("boundary");
            if (bp != null) {
               boundary = "--" + bp;
            }
         }

         if (boundary == null && !this.ignoreMissingBoundaryParameter && !this.ignoreExistingBoundaryParameter) {
            throw new MessagingException("Missing boundary parameter");
         } else {
            label677: {
               try {
                  LineInputStream lin = new LineInputStream(in);
                  StringBuffer preamblesb = null;
                  String lineSeparator = null;

                  String line;
                  while((line = lin.readLine()) != null) {
                     int i;
                     for(i = line.length() - 1; i >= 0; --i) {
                        char c = line.charAt(i);
                        if (c != ' ' && c != '\t') {
                           break;
                        }
                     }

                     line = line.substring(0, i + 1);
                     if (boundary != null) {
                        if (line.equals(boundary)) {
                           break;
                        }

                        if (line.length() == boundary.length() + 2 && line.startsWith(boundary) && line.endsWith("--")) {
                           line = null;
                           break;
                        }
                     } else if (line.length() > 2 && line.startsWith("--") && (line.length() <= 4 || !allDashes(line))) {
                        boundary = line;
                        break;
                     }

                     if (line.length() > 0) {
                        if (lineSeparator == null) {
                           try {
                              lineSeparator = System.getProperty("line.separator", "\n");
                           } catch (SecurityException var40) {
                              lineSeparator = "\n";
                           }
                        }

                        if (preamblesb == null) {
                           preamblesb = new StringBuffer(line.length() + 2);
                        }

                        preamblesb.append(line).append(lineSeparator);
                     }
                  }

                  if (preamblesb != null) {
                     this.preamble = preamblesb.toString();
                  }

                  if (line != null) {
                     byte[] bndbytes = ASCIIUtility.getBytes(boundary);
                     int bl = bndbytes.length;
                     int[] bcs = new int[256];

                     for(int i = 0; i < bl; ++i) {
                        bcs[bndbytes[i] & 255] = i + 1;
                     }

                     int[] gss = new int[bl];

                     label673:
                     for(int i = bl; i > 0; --i) {
                        int j;
                        for(j = bl - 1; j >= i; --j) {
                           if (bndbytes[j] != bndbytes[j - i]) {
                              continue label673;
                           }

                           gss[j - 1] = i;
                        }

                        while(j > 0) {
                           --j;
                           gss[j] = i;
                        }
                     }

                     gss[bl - 1] = 1;
                     boolean done = false;

                     while(true) {
                        if (done) {
                           break label677;
                        }

                        InternetHeaders headers = null;
                        if (sin == null) {
                           headers = this.createInternetHeaders(in);
                        } else {
                           start = sin.getPosition();

                           while((line = lin.readLine()) != null && line.length() > 0) {
                           }

                           if (line == null) {
                              if (!this.ignoreMissingEndBoundary) {
                                 throw new MessagingException("missing multipart end boundary");
                              }

                              this.complete = false;
                              break label677;
                           }
                        }

                        if (!in.markSupported()) {
                           throw new MessagingException("Stream doesn't support mark");
                        }

                        ByteArrayOutputStream buf = null;
                        if (sin == null) {
                           buf = new ByteArrayOutputStream();
                        } else {
                           end = sin.getPosition();
                        }

                        byte[] inbuf = new byte[bl];
                        byte[] previnbuf = new byte[bl];
                        int inSize = 0;
                        int prevSize = 0;
                        boolean first = true;

                        int eolLen;
                        while(true) {
                           in.mark(bl + 4 + 1000);
                           eolLen = 0;
                           inSize = readFully(in, inbuf, 0, bl);
                           if (inSize < bl) {
                              if (!this.ignoreMissingEndBoundary) {
                                 throw new MessagingException("missing multipart end boundary");
                              }

                              if (sin != null) {
                                 end = sin.getPosition();
                              }

                              this.complete = false;
                              done = true;
                              break;
                           }

                           int i;
                           for(i = bl - 1; i >= 0 && inbuf[i] == bndbytes[i]; --i) {
                           }

                           if (i < 0) {
                              eolLen = 0;
                              if (!first) {
                                 int b = previnbuf[prevSize - 1];
                                 if (b == 13 || b == 10) {
                                    eolLen = 1;
                                    if (b == 10 && prevSize >= 2) {
                                       b = previnbuf[prevSize - 2];
                                       if (b == 13) {
                                          eolLen = 2;
                                       }
                                    }
                                 }
                              }

                              if (first || eolLen > 0) {
                                 if (sin != null) {
                                    end = sin.getPosition() - (long)bl - (long)eolLen;
                                 }

                                 int b2 = in.read();
                                 if (b2 == 45 && in.read() == 45) {
                                    this.complete = true;
                                    done = true;
                                    break;
                                 }

                                 while(b2 == 32 || b2 == 9) {
                                    b2 = in.read();
                                 }

                                 if (b2 == 10) {
                                    break;
                                 }

                                 if (b2 == 13) {
                                    in.mark(1);
                                    if (in.read() != 10) {
                                       in.reset();
                                    }
                                    break;
                                 }
                              }

                              i = 0;
                           }

                           int skip = Math.max(i + 1 - bcs[inbuf[i] & 127], gss[i]);
                           if (skip < 2) {
                              if (sin == null && prevSize > 1) {
                                 buf.write(previnbuf, 0, prevSize - 1);
                              }

                              in.reset();
                              this.skipFully(in, 1L);
                              if (prevSize >= 1) {
                                 previnbuf[0] = previnbuf[prevSize - 1];
                                 previnbuf[1] = inbuf[0];
                                 prevSize = 2;
                              } else {
                                 previnbuf[0] = inbuf[0];
                                 prevSize = 1;
                              }
                           } else {
                              if (prevSize > 0 && sin == null) {
                                 buf.write(previnbuf, 0, prevSize);
                              }

                              prevSize = skip;
                              in.reset();
                              this.skipFully(in, (long)skip);
                              byte[] tmp = inbuf;
                              inbuf = previnbuf;
                              previnbuf = tmp;
                           }

                           first = false;
                        }

                        MimeBodyPart part;
                        if (sin != null) {
                           part = this.createMimeBodyPartIs(sin.newStream(start, end));
                        } else {
                           if (prevSize - eolLen > 0) {
                              buf.write(previnbuf, 0, prevSize - eolLen);
                           }

                           if (!this.complete && inSize > 0) {
                              buf.write(inbuf, 0, inSize);
                           }

                           part = this.createMimeBodyPart(headers, buf.toByteArray());
                        }

                        super.addBodyPart(part);
                     }
                  }

                  if (!this.allowEmpty) {
                     throw new MessagingException("Missing start boundary");
                  }
               } catch (IOException ioex) {
                  throw new MessagingException("IO Error", ioex);
               } finally {
                  try {
                     in.close();
                  } catch (IOException var39) {
                  }

               }

               return;
            }

            this.parsed = true;
         }
      }
   }

   private static boolean allDashes(String s) {
      for(int i = 0; i < s.length(); ++i) {
         if (s.charAt(i) != '-') {
            return false;
         }
      }

      return true;
   }

   private static int readFully(InputStream in, byte[] buf, int off, int len) throws IOException {
      if (len == 0) {
         return 0;
      } else {
         int total;
         int bsize;
         for(total = 0; len > 0; len -= bsize) {
            bsize = in.read(buf, off, len);
            if (bsize <= 0) {
               break;
            }

            off += bsize;
            total += bsize;
         }

         return total > 0 ? total : -1;
      }
   }

   private void skipFully(InputStream in, long offset) throws IOException {
      while(offset > 0L) {
         long cur = in.skip(offset);
         if (cur <= 0L) {
            throw new EOFException("can't skip");
         }

         offset -= cur;
      }

   }

   protected InternetHeaders createInternetHeaders(InputStream is) throws MessagingException {
      return new InternetHeaders(is);
   }

   protected MimeBodyPart createMimeBodyPart(InternetHeaders headers, byte[] content) throws MessagingException {
      return new MimeBodyPart(headers, content);
   }

   protected MimeBodyPart createMimeBodyPart(InputStream is) throws MessagingException {
      return new MimeBodyPart(is);
   }

   private MimeBodyPart createMimeBodyPartIs(InputStream is) throws MessagingException {
      MimeBodyPart var3;
      try {
         var3 = this.createMimeBodyPart(is);
      } finally {
         try {
            is.close();
         } catch (IOException var8) {
         }

      }

      return var3;
   }
}
