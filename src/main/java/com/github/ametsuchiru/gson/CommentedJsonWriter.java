package com.github.ametsuchiru.gson;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CommentedJsonWriter extends JsonWriter {

    protected final WrappedWriter out;
    protected final List<String> commentStack = new ArrayList<>();
    private final Field indentField, stackSizeField;

    private boolean commentsQueued = false;

    public CommentedJsonWriter(Writer out) {
        super(out = new WrappedWriter(out));
        this.out = (WrappedWriter) out;
        this.out.jsonWriter = this;
        try {
            this.indentField = JsonWriter.class.getDeclaredField("indent");
            this.stackSizeField = JsonWriter.class.getDeclaredField("stackSize");
            this.indentField.setAccessible(true);
            this.stackSizeField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public CommentedJsonWriter hashComment(String comment) {
        this.commentStack.add("# " + comment);
        this.commentsQueued = true;
        return this;
    }

    public CommentedJsonWriter hashComments(String... comments) {
        for (String string : comments) {
            this.commentStack.add("# " + string);
        }
        this.commentsQueued = true;
        return this;
    }

    public CommentedJsonWriter hashComments(List<String> comments) {
        for (String string : comments) {
            this.commentStack.add("# " + string);
        }
        this.commentsQueued = true;
        return this;
    }

    public CommentedJsonWriter doubleSlashComment(String comment) {
        this.commentStack.add("// " + comment);
        this.commentsQueued = true;
        return this;
    }

    public CommentedJsonWriter doubleSlashComments(String... comments) {
        for (String string : comments) {
            this.commentStack.add("// " + string);
        }
        this.commentsQueued = true;
        return this;
    }

    public CommentedJsonWriter doubleSlashComments(List<String> comments) {
        for (String string : comments) {
            this.commentStack.add("// " + string);
        }
        this.commentsQueued = true;
        return this;
    }

    @Override
    public JsonWriter name(String name) throws IOException {
        if (this.commentsQueued) {
            this.commentsQueued = false;
        }
        return super.name(name);
    }

    @Override
    public JsonWriter beginArray() throws IOException {
        this.out.watch = true;
        super.beginArray();
        this.out.watch = false;
        return this;
    }

    @Override
    public JsonWriter beginObject() throws IOException {
        this.out.watch = true;
        super.beginObject();
        this.out.watch = false;
        return this;
    }

    @Override
    public JsonWriter endArray() throws IOException {
        if (this.commentsQueued) {
            throw new IllegalStateException("Cannot place comments before an array termination!");
        }
        return super.endArray();
    }

    @Override
    public JsonWriter endObject() throws IOException {
        if (this.commentsQueued) {
            throw new IllegalStateException("Cannot place comments before an object termination!");
        }
        return super.endObject();
    }

    @Override
    public JsonWriter value(String value) throws IOException {
        if (this.commentsQueued) {
            throw new IllegalStateException("Cannot place comments before an array value!");
        }
        this.out.watch = true;
        super.value(value);
        this.out.watch = false;
        return this;
    }

    @Override
    public JsonWriter nullValue() throws IOException {
        if (this.commentsQueued) {
            throw new IllegalStateException("Cannot place comments before an array value!");
        }
        this.out.watch = true;
        super.nullValue();
        this.out.watch = false;
        return this;
    }

    @Override
    public JsonWriter value(boolean value) throws IOException {
        if (this.commentsQueued) {
            throw new IllegalStateException("Cannot place comments before an array value!");
        }
        this.out.watch = true;
        super.value(value);
        this.out.watch = false;
        return this;
    }

    @Override
    public JsonWriter value(Boolean value) throws IOException {
        if (this.commentsQueued) {
            throw new IllegalStateException("Cannot place comments before an array value!");
        }
        this.out.watch = true;
        super.value(value);
        this.out.watch = false;
        return this;
    }

    @Override
    public JsonWriter value(float value) throws IOException {
        if (this.commentsQueued) {
            throw new IllegalStateException("Cannot place comments before an array value!");
        }
        this.out.watch = true;
        super.value(value);
        this.out.watch = false;
        return this;
    }

    @Override
    public JsonWriter value(double value) throws IOException {
        if (this.commentsQueued) {
            throw new IllegalStateException("Cannot place comments before an array value!");
        }
        this.out.watch = true;
        super.value(value);
        this.out.watch = false;
        return this;
    }

    @Override
    public JsonWriter value(long value) throws IOException {
        if (this.commentsQueued) {
            throw new IllegalStateException("Cannot place comments before an array value!");
        }
        this.out.watch = true;
        super.value(value);
        this.out.watch = false;
        return this;
    }

    @Override
    public JsonWriter value(Number value) throws IOException {
        if (this.commentsQueued) {
            throw new IllegalStateException("Cannot place comments before an array value!");
        }
        this.out.watch = true;
        super.value(value);
        this.out.watch = false;
        return this;
    }

    private String getIndent() {
        try {
            int stackSize = this.stackSizeField.getInt(this);
            if (stackSize <= 1) {
                return "";
            }
            if (stackSize == 2) {
                return (String) this.indentField.get(this);
            }
            StringBuilder indents = new StringBuilder();
            String indent = (String) this.indentField.get(this);
            for (int i = 1; i < stackSize; i++) {
                indents.append(indent);
            }
            return indents.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class WrappedWriter extends Writer {

        private final Writer writer;

        private CommentedJsonWriter jsonWriter;
        private boolean header = true;
        private boolean watch = false;

        private WrappedWriter(Writer writer) {
            this.writer = writer;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (this.header) {
                this.writeComments();
                this.header = false;
            } else if (this.watch) {
                if (cbuf[0] == '"') {
                    this.writeComments();
                }
            }
            this.writer.write(cbuf, off, len);
        }

        @Override
        public void flush() throws IOException {
            this.writer.flush();
        }

        @Override
        public void close() throws IOException {
            this.writer.close();
        }

        private void writeComments() throws IOException {
            String append = "\n";
            if (!this.header) {
                append += this.jsonWriter.getIndent();
            }
            Iterator<String> iter = this.jsonWriter.commentStack.iterator();
            while (iter.hasNext()) {
                this.writer.write(iter.next() + append);
                iter.remove();
            }
        }

    }

}
