package org.mozilla.javascript.regexp;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.RegExpProxy;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

public class RegExpImpl implements RegExpProxy {
   protected String input;
   protected boolean multiline;
   protected SubString[] parens;
   protected SubString lastMatch;
   protected SubString lastParen;
   protected SubString leftContext;
   protected SubString rightContext;

   public RegExpImpl() {
      super();
   }

   public boolean isRegExp(Scriptable obj) {
      return obj instanceof NativeRegExp;
   }

   public Object compileRegExp(Context cx, String source, String flags) {
      return NativeRegExp.compileRE(cx, source, flags, false);
   }

   public Scriptable wrapRegExp(Context cx, Scriptable scope, Object compiled) {
      return new NativeRegExp(scope, (RECompiled)compiled);
   }

   public Object action(Context cx, Scriptable scope, Scriptable thisObj, Object[] args, int actionType) {
      GlobData data = new GlobData();
      data.mode = actionType;
      switch (actionType) {
         case 1:
            data.optarg = 1;
            Object rval = matchOrReplace(cx, scope, thisObj, args, this, data, false);
            return data.arrayobj == null ? rval : data.arrayobj;
         case 2:
            Object arg1 = args.length < 2 ? Undefined.instance : args[1];
            String repstr = null;
            Function lambda = null;
            if (arg1 instanceof Function) {
               lambda = (Function)arg1;
            } else {
               repstr = ScriptRuntime.toString(arg1);
            }

            data.optarg = 2;
            data.lambda = lambda;
            data.repstr = repstr;
            data.dollar = repstr == null ? -1 : repstr.indexOf(36);
            data.charBuf = null;
            data.leftIndex = 0;
            Object val = matchOrReplace(cx, scope, thisObj, args, this, data, true);
            if (data.charBuf == null) {
               if (data.global || val == null || !val.equals(Boolean.TRUE)) {
                  return data.str;
               }

               SubString lc = this.leftContext;
               replace_glob(data, cx, scope, this, lc.index, lc.length);
            }

            SubString rc = this.rightContext;
            data.charBuf.append(rc.str, rc.index, rc.index + rc.length);
            return data.charBuf.toString();
         case 3:
            data.optarg = 1;
            return matchOrReplace(cx, scope, thisObj, args, this, data, false);
         default:
            throw Kit.codeBug();
      }
   }

   private static Object matchOrReplace(Context cx, Scriptable scope, Scriptable thisObj, Object[] args, RegExpImpl reImpl, GlobData data, boolean forceFlat) {
      String str = ScriptRuntime.toString(thisObj);
      data.str = str;
      Scriptable topScope = ScriptableObject.getTopLevelScope(scope);
      NativeRegExp re;
      if (args.length == 0) {
         RECompiled compiled = NativeRegExp.compileRE(cx, "", "", false);
         re = new NativeRegExp(topScope, compiled);
      } else if (args[0] instanceof NativeRegExp) {
         re = (NativeRegExp)args[0];
      } else {
         String src = ScriptRuntime.toString(args[0]);
         String opt;
         if (data.optarg < args.length) {
            args[0] = src;
            opt = ScriptRuntime.toString(args[data.optarg]);
         } else {
            opt = null;
         }

         RECompiled compiled = NativeRegExp.compileRE(cx, src, opt, forceFlat);
         re = new NativeRegExp(topScope, compiled);
      }

      data.global = (re.getFlags() & 1) != 0;
      int[] indexp = new int[]{0};
      Object result = null;
      if (data.mode == 3) {
         result = re.executeRegExp(cx, scope, reImpl, str, indexp, 0);
         if (result != null && result.equals(Boolean.TRUE)) {
            result = reImpl.leftContext.length;
         } else {
            result = -1;
         }
      } else if (data.global) {
         re.lastIndex = (double)0.0F;

         for(int count = 0; indexp[0] <= str.length(); ++count) {
            result = re.executeRegExp(cx, scope, reImpl, str, indexp, 0);
            if (result == null || !result.equals(Boolean.TRUE)) {
               break;
            }

            if (data.mode == 1) {
               match_glob(data, cx, scope, count, reImpl);
            } else {
               if (data.mode != 2) {
                  Kit.codeBug();
               }

               SubString lastMatch = reImpl.lastMatch;
               int leftIndex = data.leftIndex;
               int leftlen = lastMatch.index - leftIndex;
               data.leftIndex = lastMatch.index + lastMatch.length;
               replace_glob(data, cx, scope, reImpl, leftIndex, leftlen);
            }

            if (reImpl.lastMatch.length == 0) {
               if (indexp[0] == str.length()) {
                  break;
               }

               int var10002 = indexp[0]++;
            }
         }
      } else {
         result = re.executeRegExp(cx, scope, reImpl, str, indexp, data.mode == 2 ? 0 : 1);
      }

      return result;
   }

   public int find_split(Context cx, Scriptable scope, String target, String separator, Scriptable reObj, int[] ip, int[] matchlen, boolean[] matched, String[][] parensp) {
      int i = ip[0];
      int length = target.length();
      int version = cx.getLanguageVersion();
      NativeRegExp re = (NativeRegExp)reObj;

      int result;
      while(true) {
         int ipsave = ip[0];
         ip[0] = i;
         Object ret = re.executeRegExp(cx, scope, this, target, ip, 0);
         if (ret != Boolean.TRUE) {
            ip[0] = ipsave;
            matchlen[0] = 1;
            matched[0] = false;
            return length;
         }

         i = ip[0];
         ip[0] = ipsave;
         matched[0] = true;
         SubString sep = this.lastMatch;
         matchlen[0] = sep.length;
         if (matchlen[0] != 0 || i != ip[0]) {
            result = i - matchlen[0];
            break;
         }

         if (i == length) {
            if (version == 120) {
               matchlen[0] = 1;
               result = i;
            } else {
               result = -1;
            }
            break;
         }

         ++i;
      }

      int size = this.parens == null ? 0 : this.parens.length;
      parensp[0] = new String[size];

      for(int num = 0; num < size; ++num) {
         SubString parsub = this.getParenSubString(num);
         parensp[0][num] = parsub.toString();
      }

      return result;
   }

   SubString getParenSubString(int i) {
      if (this.parens != null && i < this.parens.length) {
         SubString parsub = this.parens[i];
         if (parsub != null) {
            return parsub;
         }
      }

      return SubString.emptySubString;
   }

   private static void match_glob(GlobData mdata, Context cx, Scriptable scope, int count, RegExpImpl reImpl) {
      if (mdata.arrayobj == null) {
         mdata.arrayobj = cx.newArray(scope, 0);
      }

      SubString matchsub = reImpl.lastMatch;
      String matchstr = matchsub.toString();
      mdata.arrayobj.put(count, mdata.arrayobj, matchstr);
   }

   private static void replace_glob(GlobData rdata, Context cx, Scriptable scope, RegExpImpl reImpl, int leftIndex, int leftlen) {
      String lambdaStr;
      int replen;
      if (rdata.lambda != null) {
         SubString[] parens = reImpl.parens;
         int parenCount = parens == null ? 0 : parens.length;
         Object[] args = new Object[parenCount + 3];
         args[0] = reImpl.lastMatch.toString();

         for(int i = 0; i < parenCount; ++i) {
            SubString sub = parens[i];
            if (sub != null) {
               args[i + 1] = sub.toString();
            } else {
               args[i + 1] = Undefined.instance;
            }
         }

         args[parenCount + 1] = reImpl.leftContext.length;
         args[parenCount + 2] = rdata.str;
         if (reImpl != ScriptRuntime.getRegExpProxy(cx)) {
            Kit.codeBug();
         }

         RegExpImpl re2 = new RegExpImpl();
         re2.multiline = reImpl.multiline;
         re2.input = reImpl.input;
         ScriptRuntime.setRegExpProxy(cx, re2);

         try {
            Scriptable parent = ScriptableObject.getTopLevelScope(scope);
            Object result = rdata.lambda.call(cx, parent, parent, args);
            lambdaStr = ScriptRuntime.toString(result);
         } finally {
            ScriptRuntime.setRegExpProxy(cx, reImpl);
         }

         replen = lambdaStr.length();
      } else {
         lambdaStr = null;
         replen = rdata.repstr.length();
         if (rdata.dollar >= 0) {
            int[] skip = new int[1];
            int dp = rdata.dollar;

            do {
               SubString sub = interpretDollar(cx, reImpl, rdata.repstr, dp, skip);
               if (sub != null) {
                  replen += sub.length - skip[0];
                  dp += skip[0];
               } else {
                  ++dp;
               }

               dp = rdata.repstr.indexOf(36, dp);
            } while(dp >= 0);
         }
      }

      int growth = leftlen + replen + reImpl.rightContext.length;
      StringBuilder charBuf = rdata.charBuf;
      if (charBuf == null) {
         charBuf = new StringBuilder(growth);
         rdata.charBuf = charBuf;
      } else {
         charBuf.ensureCapacity(rdata.charBuf.length() + growth);
      }

      charBuf.append(reImpl.leftContext.str, leftIndex, leftIndex + leftlen);
      if (rdata.lambda != null) {
         charBuf.append(lambdaStr);
      } else {
         do_replace(rdata, cx, reImpl);
      }

   }

   private static SubString interpretDollar(Context cx, RegExpImpl res, String da, int dp, int[] skip) {
      if (da.charAt(dp) != '$') {
         Kit.codeBug();
      }

      int version = cx.getLanguageVersion();
      if (version != 0 && version <= 140 && dp > 0 && da.charAt(dp - 1) == '\\') {
         return null;
      } else {
         int daL = da.length();
         if (dp + 1 >= daL) {
            return null;
         } else {
            char dc = da.charAt(dp + 1);
            if (!NativeRegExp.isDigit(dc)) {
               skip[0] = 2;
               switch (dc) {
                  case '$':
                     return new SubString("$");
                  case '&':
                     return res.lastMatch;
                  case '\'':
                     return res.rightContext;
                  case '+':
                     return res.lastParen;
                  case '`':
                     if (version == 120) {
                        res.leftContext.index = 0;
                        res.leftContext.length = res.lastMatch.index;
                     }

                     return res.leftContext;
                  default:
                     return null;
               }
            } else {
               int num;
               int cp;
               if (version != 0 && version <= 140) {
                  if (dc == '0') {
                     return null;
                  }

                  num = 0;
                  cp = dp;

                  while(true) {
                     ++cp;
                     if (cp >= daL || !NativeRegExp.isDigit(dc = da.charAt(cp))) {
                        break;
                     }

                     int tmp = 10 * num + (dc - 48);
                     if (tmp < num) {
                        break;
                     }

                     num = tmp;
                  }
               } else {
                  int parenCount = res.parens == null ? 0 : res.parens.length;
                  num = dc - 48;
                  if (num > parenCount) {
                     return null;
                  }

                  cp = dp + 2;
                  if (dp + 2 < daL) {
                     dc = da.charAt(dp + 2);
                     if (NativeRegExp.isDigit(dc)) {
                        int tmp = 10 * num + (dc - 48);
                        if (tmp <= parenCount) {
                           ++cp;
                           num = tmp;
                        }
                     }
                  }

                  if (num == 0) {
                     return null;
                  }
               }

               --num;
               skip[0] = cp - dp;
               return res.getParenSubString(num);
            }
         }
      }
   }

   private static void do_replace(GlobData rdata, Context cx, RegExpImpl regExpImpl) {
      StringBuilder charBuf = rdata.charBuf;
      int cp = 0;
      String da = rdata.repstr;
      int dp = rdata.dollar;
      if (dp != -1) {
         int[] skip = new int[1];

         do {
            int var10000 = dp - cp;
            charBuf.append(da.substring(cp, dp));
            cp = dp;
            SubString sub = interpretDollar(cx, regExpImpl, da, dp, skip);
            if (sub != null) {
               int len = sub.length;
               if (len > 0) {
                  charBuf.append(sub.str, sub.index, sub.index + len);
               }

               cp = dp + skip[0];
               dp += skip[0];
            } else {
               ++dp;
            }

            dp = da.indexOf(36, dp);
         } while(dp >= 0);
      }

      int daL = da.length();
      if (daL > cp) {
         charBuf.append(da.substring(cp, daL));
      }

   }

   public Object js_split(Context cx, Scriptable scope, String target, Object[] args) {
      Scriptable result = cx.newArray(scope, 0);
      if (args.length < 1) {
         result.put(0, result, target);
         return result;
      } else {
         boolean limited = args.length > 1 && args[1] != Undefined.instance;
         long limit = 0L;
         if (limited) {
            limit = ScriptRuntime.toUint32(args[1]);
            if (limit > (long)target.length()) {
               limit = (long)(1 + target.length());
            }
         }

         String separator = null;
         int[] matchlen = new int[1];
         Scriptable re = null;
         RegExpProxy reProxy = null;
         if (args[0] instanceof Scriptable) {
            reProxy = ScriptRuntime.getRegExpProxy(cx);
            if (reProxy != null) {
               Scriptable test = (Scriptable)args[0];
               if (reProxy.isRegExp(test)) {
                  re = test;
               }
            }
         }

         if (re == null) {
            separator = ScriptRuntime.toString(args[0]);
            matchlen[0] = separator.length();
         }

         int[] ip = new int[]{0};
         int len = 0;
         boolean[] matched = new boolean[]{false};
         String[][] parens = new String[][]{null};
         int version = cx.getLanguageVersion();

         int match;
         while((match = find_split(cx, scope, target, separator, version, reProxy, re, ip, matchlen, matched, parens)) >= 0 && (!limited || (long)len < limit) && match <= target.length()) {
            String substr;
            if (target.length() == 0) {
               substr = target;
            } else {
               substr = target.substring(ip[0], match);
            }

            result.put(len, result, substr);
            ++len;
            if (re != null && matched[0]) {
               int size = parens[0].length;

               for(int num = 0; num < size && (!limited || (long)len < limit); ++num) {
                  result.put(len, result, parens[0][num]);
                  ++len;
               }

               matched[0] = false;
            }

            ip[0] = match + matchlen[0];
            if (version < 130 && version != 0 && !limited && ip[0] == target.length()) {
               break;
            }
         }

         return result;
      }
   }

   private static int find_split(Context cx, Scriptable scope, String target, String separator, int version, RegExpProxy reProxy, Scriptable re, int[] ip, int[] matchlen, boolean[] matched, String[][] parensp) {
      int i = ip[0];
      int length = target.length();
      if (version == 120 && re == null && separator.length() == 1 && separator.charAt(0) == ' ') {
         if (i == 0) {
            while(i < length && Character.isWhitespace(target.charAt(i))) {
               ++i;
            }

            ip[0] = i;
         }

         if (i == length) {
            return -1;
         } else {
            while(i < length && !Character.isWhitespace(target.charAt(i))) {
               ++i;
            }

            int j;
            for(j = i; j < length && Character.isWhitespace(target.charAt(j)); ++j) {
            }

            matchlen[0] = j - i;
            return i;
         }
      } else if (i > length) {
         return -1;
      } else if (re != null) {
         return reProxy.find_split(cx, scope, target, separator, re, ip, matchlen, matched, parensp);
      } else if (version != 0 && version < 130 && length == 0) {
         return -1;
      } else if (separator.length() == 0) {
         if (version == 120) {
            if (i == length) {
               matchlen[0] = 1;
               return i;
            } else {
               return i + 1;
            }
         } else {
            return i == length ? -1 : i + 1;
         }
      } else if (ip[0] >= length) {
         return length;
      } else {
         i = target.indexOf(separator, ip[0]);
         return i != -1 ? i : length;
      }
   }
}
