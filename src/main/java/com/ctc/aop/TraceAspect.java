package com.ctc.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.annotation.AfterReturning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aspectj.lang.JoinPoint;

@Aspect
public class TraceAspect {

    private int indent = 0;
    private List<String> indents = new ArrayList<>();
    private static final Set<String> renderableTypes = new HashSet<>(Arrays.asList(
            javax.xml.namespace.QName.class.getName(),
            int.class.getName(),
            long.class.getName(),
            byte.class.getName(),
            boolean.class.getName(),
            Integer.class.getName(),
            Long.class.getName(),
            Byte.class.getName(),
            Boolean.class.getName()
            ));

    @Before("execution(* com.ctc.wstx..*.*(..)) && !handler(*) && !execution(* com.ctc.wstx.util.XmlChars.*(..))")
    public void beforeMethodCall(JoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof CodeSignature) {
            CodeSignature sig = (CodeSignature) joinPoint.getSignature();
            System.out.println(indent() + "> " + sig.getDeclaringTypeName() + "." + sig.getName() + "("+
            params(sig.getParameterTypes(), joinPoint.getArgs()) +")");
        }
    }

    @AfterReturning(pointcut = "execution(* com.ctc.wstx..*.*(..)) && !handler(*) && !execution(* com.ctc.wstx.util.XmlChars.*(..))", returning = "returnValue")
    //@After("execution(* com.ctc.wstx..*.*(..)) && !handler(*) && !execution(* com.ctc.wstx.util.XmlChars.*(..))")
    public void afterMethodCall(JoinPoint joinPoint, Object returnValue) {
        if (joinPoint.getSignature() instanceof CodeSignature) {
            CodeSignature sig = (CodeSignature) joinPoint.getSignature();
            StringBuilder ret = new StringBuilder();
            String returnType = (sig instanceof MethodSignature) ? ((MethodSignature) sig).getReturnType().getName() : "void";
            param(ret, returnValue, returnType);
            System.out.println(unindent() + "< " + sig.getDeclaringTypeName() + "." + sig.getName() + "("+
                    params(sig.getParameterTypes(), joinPoint.getArgs()) +") : " + ret.toString());
        }
    }

    static String params(Class<?>[] types, Object[] args) {
        if (types.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Object val = args[i];
            final String type = types[i].getName();
            param(sb, val, type);
        }
        return sb.toString();
    }

    private static void param(StringBuilder sb, Object val, final String type) {
        if (val == null) {
            sb.append("null").append(':').append(type);
        } else if (val.getClass() == String.class) {
            escapeString('"', (String) val, sb);
            sb.append(':').append(type);
        } else if (val != null && (val.getClass() == char.class || val.getClass() == Character.class)) {
            escapeChar('\'', (Character) val, sb);
            sb.append(':').append(type);
        } else if (renderableTypes.contains(val.getClass().getName())) {
            sb.append(val).append(':').append(type);
        } else {
            sb.append(type);
        }
    }
    static void escapeString(char quote, String input, StringBuilder escapedString) {
        escapedString.append(quote);
        for (char c : input.toCharArray()) {
            switch (c) {
                case '\n':
                    escapedString.append("\\n");
                    break;
                case '\r':
                    escapedString.append("\\r");
                    break;
                case '\t':
                    escapedString.append("\\t");
                    break;
                case '\"':
                    escapedString.append("\\\"");
                    break;
                case '\\':
                    escapedString.append("\\\\");
                    break;
                // Add more cases here for other special characters if needed
                default:
                    escapedString.append(c);
            }
        }
        escapedString.append(quote);
    }

    static void escapeChar(char quote, Character c, StringBuilder escapedString) {
        escapedString.append(quote);
            switch (c.charValue()) {
                case '\n':
                    escapedString.append("\\n");
                    break;
                case '\r':
                    escapedString.append("\\r");
                    break;
                case '\t':
                    escapedString.append("\\t");
                    break;
                case '\"':
                    escapedString.append("\\\"");
                    break;
                case '\\':
                    escapedString.append("\\\\");
                    break;
                // Add more cases here for other special characters if needed
                default:
                    escapedString.append(c);
            }
        escapedString.append(quote);
    }

    private String indent() {
        while (indent >= indents.size()) {
            indents.add(spaces(indents.size()));
        }
        return indents.get(indent++);
    }

    private String unindent() {
        return indents.get(--indent);
    }

    static String spaces(int size) {
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }
}