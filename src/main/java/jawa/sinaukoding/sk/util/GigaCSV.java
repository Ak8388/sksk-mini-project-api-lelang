package jawa.sinaukoding.sk.util;

//
// Copyright (c) 2024 - Ardika Rommy Sanjaya
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

// package your.package;

/**
 * GigaCSV is a tools for reading/writing large CSV file efficiently.
 *
 * <p>
 */
public final class GigaCSV {

    private static final boolean PRINT_STACK_TRACE =
        Boolean.parseBoolean(System.getProperty("gigacsv.stackTrace", "true"));
  
    private static final byte LINE_BREAK_CR = '\r';
    private static final byte LINE_BREAK_LF = '\n';
  
    private final Option option;
  
    /**
     * Create GigaCSV instance.
     *
     * <p>
     */
    public GigaCSV() {
      this(new Option());
    }
  
    /**
     * Create GigaCSV instance.
     *
     * @param option config.
     */
    public GigaCSV(final Option option) {
      this.option = option;
    }
  
    /**
     * Read CSV file.
     *
     * <p>
     *
     * <pre>
     * final GigaCSV gigaCSV = new GigaCSV();
     * try (final GigaCSV.Reader reader = gigaCSV.reader("file.csv")) {
     *     for (final GigaCSV.Record record : reader) {
     *         //
     *     }
     * } catch (Exception e) {
     *     //
     * }
     * </pre>
     *
     * @param path file path.
     * @return returns {@link Reader}.
     * @throws java.io.FileNotFoundException file not found.
     * @throws java.lang.Exception unable to close file.
     */
    public final Reader reader(final String path) throws java.io.FileNotFoundException {
      return new Reader(path, option);
    }
  
    /**
     * Write to CSV file.
     *
     * <p>
     *
     * <pre>
     * try (final GigaCSV.Writer writer = gigaCSV.writer("file.csv")) {
     *     writer.write(...);
     * } catch(Exception e) {
     *     //
     * }
     * </pre>
     * @param path file path.
     * @return returns {@link Writer}.
     * @throws java.io.FileNotFoundException file not found.
     * @throws java.lang.Exception unable to close file.
     */
    public final Writer writer(final String path) throws java.io.FileNotFoundException {
      return new Writer(path, option);
    }
  
    /**
     * CSV writer.
     *
     * <p>
     */
    public static final class Writer implements AutoCloseable {
  
      private final java.io.RandomAccessFile sequentialAccessFile;
      private final byte[] buffer;
      private final byte quote;
      private final byte separator;
  
      private Writer(String path, Option option) throws java.io.FileNotFoundException {
        this.sequentialAccessFile = new java.io.RandomAccessFile(path, "rw");
        this.buffer = new byte[option.bufferSize];
        this.quote = option.quote;
        this.separator = option.separator;
      }
  
      public final int write(java.lang.Iterable<Record> records) throws java.io.IOException {
        int written = 0;
        for (final Record record : records) {
          System.out.println(record);
          written += write(record);
        }
        return written;
      }
  
      public final int write(Record... records) throws java.io.IOException {
        int written = 0;
        int idx = 0;
        int checkpoint = 0;
        for (int i = 0; i < records.length; i++) {
          f:
          for (int j = 0; j < records[i].fields.length; j++) {
            final byte[] bytes =
                records[i].fields[j].getBytes(java.nio.charset.StandardCharsets.UTF_8);
            for (int c = 0; c < bytes.length; c++) {
              final byte b = bytes[c];
              final char z = (char) b;
              final String y = records[i].fields[j];
              if (((b & 0x80) == 0)) {
                if (b == quote) {
                  if (idx + 2 == buffer.length) {
                    written += flush(checkpoint);
                    i -= 1;
                    idx = 0;
                    break f;
                  } else {
                    buffer[idx++] = quote;
                    buffer[idx++] = b;
                  }
                } else {
                  if (idx + 1 == buffer.length) {
                    written += flush(checkpoint);
                    i -= 1;
                    idx = 0;
                    break f;
                  } else {
                    buffer[idx++] = b;
                  }
                }
              } else if ((b & 0xf0) == 0xf0) { // four bytes unicode
                if (idx + 4 == buffer.length) {
                  written += flush(checkpoint);
                  i -= 1;
                  idx = 0;
                  break f;
                } else {
                  buffer[idx++] = bytes[c++];
                  buffer[idx++] = bytes[c++];
                  buffer[idx++] = bytes[c++];
                  buffer[idx++] = bytes[c++];
                }
              } else if ((b & 0xe0) == 0xe0) { // three bytes unicode
                if (idx + 3 == buffer.length) {
                  written += flush(checkpoint);
                  i -= 1;
                  idx = 0;
                  break f;
                } else {
                  buffer[idx++] = bytes[c++];
                  buffer[idx++] = bytes[c++];
                  buffer[idx++] = bytes[c++];
                }
              } else if ((b & 0xc0) == 0xc0) { // two bytes unicode
                if (idx + 2 == buffer.length) {
                  written += flush(checkpoint);
                  i -= 1;
                  idx = 0;
                  break f;
                } else {
                  buffer[idx++] = bytes[c++];
                  buffer[idx++] = bytes[c++];
                }
              } else {
                throw new java.io.UTFDataFormatException("Invalid csv fields");
              }
            }
            if (j < records[i].fields.length - 1) {
              if (idx + 1 == buffer.length) {
                written += flush(checkpoint);
                i -= 1;
                idx = 0;
                break f;
              } else {
                buffer[idx++] = separator;
              }
            }
          }
          if (idx > 0) {
            if (records[i].lineBreak == LineBreak.CRLF) {
              if (idx + 2 == buffer.length) {
                written += flush(checkpoint);
                i -= 1;
                idx = 0;
              } else {
                buffer[idx++] = LINE_BREAK_CR;
                buffer[idx++] = LINE_BREAK_LF;
              }
            } else if (records[i].lineBreak == LineBreak.LF) {
              if (idx + 1 == buffer.length) {
                written += flush(checkpoint);
                i -= 1;
                idx = 0;
              } else {
                buffer[idx++] = LINE_BREAK_LF;
              }
            } else if (records[i].lineBreak == LineBreak.CR) {
              if (idx + 1 == buffer.length) {
                written += flush(checkpoint);
                i -= 1;
                idx = 0;
              } else {
                buffer[idx++] = LINE_BREAK_CR;
              }
            }
            checkpoint = idx;
          }
        }
        written += flush(checkpoint);
        return written;
      }
  
      private final int flush(int length) throws java.io.IOException {
        sequentialAccessFile.write(buffer, 0, length);
        return length;
      }
  
      @Override
      public final void close() throws Exception {
        sequentialAccessFile.close();
      }
    }
  
    /**
     * Iterable CSV reader.
     *
     * <p>
     */
    public static final class Reader implements Iterable<Record>, AutoCloseable {
  
      private final java.io.RandomAccessFile sequentialAccessFile;
      private final Option option;
  
      private Reader(final String path, final Option option) throws java.io.FileNotFoundException {
        this.sequentialAccessFile = new java.io.RandomAccessFile(path, "r");
        this.option = option;
      }
  
      /**
       * Get {@link Record} iterator.
       *
       * @return returns {@link Record} iterator.
       */
      public final java.util.Iterator<Record> iterator() {
        return new ReaderIterator(sequentialAccessFile, option);
      }
  
      /**
       * Close the CSV file.
       *
       * @throws Exception error while closing CSV file.
       */
      @Override
      public final void close() throws Exception {
        sequentialAccessFile.close();
      }
    }
  
    private static final class ReaderIterator implements java.util.Iterator<Record> {
  
      private final java.io.RandomAccessFile file;
      private long number;
      private int readLength;
      private int offset;
      private int fieldSize;
      private final byte[] buffer;
      private final int bufferSize;
      private final int headerSize; // default header size
      private final byte quote;
      private final byte separator;
  
      //
      private Record next;
  
      private ReaderIterator(
          final java.io.RandomAccessFile sequentialAccessFile, final Option option) {
        this.file = sequentialAccessFile;
        this.number = 0;
        this.readLength = 0;
        this.offset = 0;
        this.fieldSize = 0;
        this.buffer = new byte[option.bufferSize];
        this.bufferSize = option.bufferSize;
        this.headerSize = option.headerSize;
        ;
        this.quote = option.quote;
        this.separator = option.separator;
        try {
          this.file.seek(0L);
          next();
        } catch (final java.io.IOException e) {
          if (PRINT_STACK_TRACE) {
            e.printStackTrace();
          }
          this.next = null;
        }
      }
  
      @Override
      public final boolean hasNext() {
        return next != null;
      }
  
      @Override
      public final Record next() {
        final Record previous = next;
        try {
          if (offset < readLength) {
            NextRecord record = nextRecord(offset, readLength);
            if (record.status == 0) {
              offset += record.offset + record.lineBreak.length;
              this.next = toRecord(record);
              this.number += 1;
            } else {
              if (record.status == NextRecord.BUFFER_UNDERFLOW.status) {
                final int remainingLength = readLength - offset;
                System.arraycopy(buffer, offset, buffer, 0, remainingLength);
                final int read = file.read(buffer, remainingLength, bufferSize - remainingLength);
                if (read == -1) { // end of file
                  readLength = remainingLength;
                } else {
                  readLength = remainingLength + read;
                }
                if (readLength < 1) {
                  this.next = null;
                  return previous;
                }
                offset = 0;
                record = nextRecord(offset, readLength);
                offset += record.offset + record.lineBreak.length;
                this.next = toRecord(record);
                this.number += 1;
              } else if (record.status == NextRecord.INVALID_CSV_FORMAT.status) {
                throw new java.io.UTFDataFormatException("Invalid CSV format");
              } else if (record.status == NextRecord.UNSUPPORTED_ENCODING.status) {
                throw new java.io.UTFDataFormatException("Invalid CSV file");
              } else if (record.status == NextRecord.BUFFER_OVERFLOW.status) {
                throw new ArrayIndexOutOfBoundsException("Buffer overflow");
              } else {
                throw new Exception("Unknown exception");
              }
            }
          } else {
            readLength = file.read(buffer, 0, bufferSize);
            if (readLength < 1) {
              this.next = null;
              return previous;
            }
            offset = 0;
            final NextRecord record = nextRecord(offset, readLength);
            offset += record.offset + record.lineBreak.length;
            this.next = toRecord(record);
            this.number += 1;
          }
        } catch (final java.lang.Throwable e) {
          if (PRINT_STACK_TRACE) {
            e.printStackTrace();
          }
          this.next = null;
          return previous;
        }
        return previous;
      }
  
      @Override
      public final void remove() {
        throw new UnsupportedOperationException();
      }
  
      private final Record toRecord(final NextRecord record) throws java.io.UTFDataFormatException {
        if (fieldSize == 0) {
          fieldSize = record.fields.length;
        } else {
          if (fieldSize != record.fields.length) {
            throw new java.io.UTFDataFormatException("Invalid csv file");
          }
        }
        final FieldBuf[] fieldBufs = record.fields;
        final String[] fields = new String[fieldBufs.length];
        for (int i = 0; i < fieldBufs.length; i++) {
          final FieldBuf fieldBuf = fieldBufs[i];
          final int len = fieldBuf.len;
          if (len > 0) {
            if (buffer[fieldBuf.start] == quote) {
              // assert len > 1 && buffer[fieldBuf.end - 1] == quote;
              final int newStart = fieldBuf.start + 1;
              final int newEnd = fieldBuf.end - 1;
              final byte[] newStr = new byte[len - 2];
              int index = 0;
              for (int j = newStart; j < newEnd; j++) {
                if (buffer[j] == quote) {
                  if (j > newStart && buffer[j - 1] == quote) {
                    continue;
                  } else {
                    newStr[index] = buffer[j];
                  }
                } else {
                  newStr[index] = buffer[j];
                }
                index += 1;
              }
              fields[i] = new String(newStr, 0, index, java.nio.charset.StandardCharsets.UTF_8);
            } else {
              fields[i] =
                  new String(
                      buffer, fieldBuf.start, fieldBuf.len, java.nio.charset.StandardCharsets.UTF_8);
            }
          } else {
            fields[i] =
                new String(
                    buffer, fieldBuf.start, fieldBuf.len, java.nio.charset.StandardCharsets.UTF_8);
          }
        }
        return new Record(number, fields, record.lineBreak);
      }
  
      private final NextRecord nextRecord(final int offset, final int length) {
        FieldBuf[] fieldBufs;
        if (fieldSize == 0) {
          fieldBufs = new FieldBuf[headerSize];
        } else {
          fieldBufs = new FieldBuf[fieldSize];
        }
        int fieldBufIdx = 0;
        int i = offset;
        int startField = offset;
        State state = State.START;
        while (i < length) {
          final byte b = buffer[i];
          if ((b & 0x80) == 0) { // single byte ASCII
            switch (state) {
              case START:
                if (b == quote) {
                  startField = i;
                  state = State.START_QUOTE;
                } else {
                  if (b == separator) {
                    if (fieldBufIdx == fieldBufs.length) {
                      final FieldBuf[] newCopy = new FieldBuf[fieldBufIdx << 1];
                      System.arraycopy(fieldBufs, 0, newCopy, 0, fieldBufs.length);
                      newCopy[fieldBufIdx] = new FieldBuf(startField, i);
                      fieldBufs = newCopy;
                      fieldBufIdx += 1;
                    } else {
                      fieldBufs[fieldBufIdx] = new FieldBuf(startField, i);
                      fieldBufIdx += 1;
                    }
  
                    startField = i + 1;
                    // state = State.START;
                  } else {
                    // state = State.START;
                  }
                }
                break;
              case START_QUOTE:
                if (b == quote) {
                  state = State.MUST_QUOTE;
                } /* else if (b == separator) {
                      state = State.START_QUOTE;
                  } else {
                      state = State.START_QUOTE;
                  } */
                break;
              case MUST_QUOTE:
                if (b == quote) {
                  state = State.START_QUOTE;
                } else if (b == separator) {
                  if (fieldBufIdx == fieldBufs.length) {
                    final FieldBuf[] newCopy = new FieldBuf[fieldBufIdx << 1];
                    System.arraycopy(fieldBufs, 0, newCopy, 0, fieldBufs.length);
                    newCopy[fieldBufIdx] = new FieldBuf(startField, i);
                    fieldBufs = newCopy;
                    fieldBufIdx += 1;
                  } else {
                    fieldBufs[fieldBufIdx] = new FieldBuf(startField, i);
                    fieldBufIdx += 1;
                  }
                  startField = i + 1;
                  state = State.START;
                } else if (b == LINE_BREAK_CR || b == LINE_BREAK_LF) {
                  state = State.START;
                } else {
                  return NextRecord.INVALID_CSV_FORMAT;
                }
                break;
            }
            if (b == LINE_BREAK_CR) {
              if (state == State.START_QUOTE) {
                i += 1;
              } else {
                if (i + 1 < length) {
                  if (buffer[i + 1] == LINE_BREAK_LF) {
                    if (fieldBufIdx + 1 == fieldBufs.length) {
                      fieldBufs[fieldBufIdx] = new FieldBuf(startField, i);
                      fieldSize = fieldBufs.length;
                      return new NextRecord(i - offset, fieldBufs, LineBreak.CRLF);
                    } else {
                      if (fieldBufIdx == fieldBufs.length) {
                        final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                        System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                        copy[fieldBufIdx] = new FieldBuf(startField, i);
                        fieldSize = copy.length;
                        return new NextRecord(i - offset, copy, LineBreak.CRLF);
                      } else {
                        final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                        System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                        copy[fieldBufIdx] = new FieldBuf(startField, i);
                        fieldSize = copy.length;
                        return new NextRecord(i - offset, copy, LineBreak.CRLF);
                      }
                    }
                  } else {
                    if (fieldBufIdx + 1 == fieldBufs.length) {
                      fieldBufs[fieldBufIdx] = new FieldBuf(startField, i);
                      fieldSize = fieldBufs.length;
                      return new NextRecord(i - offset, fieldBufs, LineBreak.CR);
                    } else {
                      if (fieldBufIdx == fieldBufs.length) {
                        final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                        System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                        copy[fieldBufIdx] = new FieldBuf(startField, i);
                        fieldSize = copy.length;
                        return new NextRecord(i - offset, copy, LineBreak.CR);
                      } else {
                        final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                        System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                        copy[fieldBufIdx] = new FieldBuf(startField, i);
                        fieldSize = copy.length;
                        return new NextRecord(i - offset, copy, LineBreak.CR);
                      }
                    }
                  }
                }
                if (i + 1 == length) {
                  if (fieldBufIdx + 1 == fieldBufs.length) {
                    fieldBufs[fieldBufIdx] = new FieldBuf(startField, i);
                    fieldSize = fieldBufs.length;
                    return new NextRecord(i - offset, fieldBufs, LineBreak.CR);
                  } else {
                    if (fieldBufIdx == fieldBufs.length) {
                      final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                      System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                      copy[fieldBufIdx] = new FieldBuf(startField, i);
                      fieldSize = copy.length;
                      return new NextRecord(i - offset, copy, LineBreak.CR);
                    } else {
                      final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                      System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                      copy[fieldBufIdx] = new FieldBuf(startField, i);
                      fieldSize = copy.length;
                      return new NextRecord(i - offset, copy, LineBreak.CR);
                    }
                  }
                } else {
                  return NextRecord.BUFFER_UNDERFLOW;
                }
              }
            } else if (b == LINE_BREAK_LF) {
              if (state == State.START_QUOTE) {
                i += 1;
              } else {
                if (fieldBufIdx + 1 == fieldBufs.length) {
                  fieldBufs[fieldBufIdx] = new FieldBuf(startField, i);
                  fieldSize = fieldBufs.length;
                  return new NextRecord(i - offset, fieldBufs, LineBreak.LF);
                } else {
                  if (fieldBufIdx == fieldBufs.length) {
                    final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                    System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                    copy[fieldBufIdx] = new FieldBuf(startField, i);
                    fieldSize = copy.length;
                    return new NextRecord(i - offset, copy, LineBreak.LF);
                  } else {
                    final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                    System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                    copy[fieldBufIdx] = new FieldBuf(startField, i);
                    fieldSize = copy.length;
                    return new NextRecord(i - offset, copy, LineBreak.LF);
                  }
                }
              }
            } else {
              i += 1;
            }
          } else if ((b & 0xf0) == 0xf0) { // four bytes unicode
            i += 4;
            if (i < length) {
              if (((buffer[i - 1] & 0xff) >>> 6) != 2) {
                return NextRecord.UNSUPPORTED_ENCODING;
              }
              if (((buffer[i - 2] & 0xff) >>> 6) != 2) {
                return NextRecord.UNSUPPORTED_ENCODING;
              }
              if (((buffer[i - 3] & 0xff) >>> 6) != 2) {
                return NextRecord.UNSUPPORTED_ENCODING;
              }
            } else if (i == length) {
              if (length < bufferSize) {
                if (fieldBufIdx + 1 == fieldBufs.length) {
                  fieldBufs[fieldBufIdx] = new FieldBuf(startField, i);
                  fieldSize = fieldBufs.length;
                  return new NextRecord(i - offset, fieldBufs, LineBreak.NONE);
                } else {
                  if (fieldBufIdx == fieldBufs.length) {
                    final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                    System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                    copy[fieldBufIdx] = new FieldBuf(startField, i);
                    fieldSize = copy.length;
                    return new NextRecord(i - offset, copy, LineBreak.NONE);
                  } else {
                    final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                    System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                    copy[fieldBufIdx] = new FieldBuf(startField, i);
                    fieldSize = copy.length;
                    return new NextRecord(i - offset, copy, LineBreak.NONE);
                  }
                }
              } else {
                return NextRecord.BUFFER_UNDERFLOW;
              }
            } else {
              return NextRecord.BUFFER_UNDERFLOW;
            }
          } else if ((b & 0xe0) == 0xe0) { // three bytes unicode
            i += 3;
            if (i < length) {
              if (((buffer[i - 1] & 0xff) >>> 6) != 2) {
                return NextRecord.UNSUPPORTED_ENCODING;
              }
              if (((buffer[i - 2] & 0xff) >>> 6) != 2) {
                return NextRecord.UNSUPPORTED_ENCODING;
              }
            } else if (i == length) {
              if (length < bufferSize) {
                if (fieldBufIdx + 1 == fieldBufs.length) {
                  fieldBufs[fieldBufIdx] = new FieldBuf(startField, i);
                  fieldSize = fieldBufs.length;
                  return new NextRecord(i - offset, fieldBufs, LineBreak.NONE);
                } else {
                  if (fieldBufIdx == fieldBufs.length) {
                    final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                    System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                    copy[fieldBufIdx] = new FieldBuf(startField, i);
                    fieldSize = copy.length;
                    return new NextRecord(i - offset, copy, LineBreak.NONE);
                  } else {
                    final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                    System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                    copy[fieldBufIdx] = new FieldBuf(startField, i);
                    fieldSize = copy.length;
                    return new NextRecord(i - offset, copy, LineBreak.NONE);
                  }
                }
              } else {
                return NextRecord.BUFFER_UNDERFLOW;
              }
            } else {
              return NextRecord.BUFFER_UNDERFLOW;
            }
          } else if ((b & 0xc0) == 0xc0) { // two bytes unicode
            i += 2;
            if (i < length) {
              final byte c = buffer[i - 1];
              if (((c & 0xff) >>> 6) != 2) {
                return NextRecord.UNSUPPORTED_ENCODING;
              }
            } else if (i == length) {
              if (length < bufferSize) {
                if (fieldBufIdx + 1 == fieldBufs.length) {
                  fieldBufs[fieldBufIdx] = new FieldBuf(startField, i);
                  fieldSize = fieldBufs.length;
                  return new NextRecord(i - offset, fieldBufs, LineBreak.NONE);
                } else {
                  if (fieldBufIdx == fieldBufs.length) {
                    final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                    System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                    copy[fieldBufIdx] = new FieldBuf(startField, i);
                    fieldSize = copy.length;
                    return new NextRecord(i - offset, copy, LineBreak.NONE);
                  } else {
                    final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                    System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                    copy[fieldBufIdx] = new FieldBuf(startField, i);
                    fieldSize = copy.length;
                    return new NextRecord(i - offset, copy, LineBreak.NONE);
                  }
                }
              } else {
                return NextRecord.BUFFER_UNDERFLOW;
              }
            } else {
              return NextRecord.BUFFER_UNDERFLOW;
            }
          } else {
            return NextRecord.UNSUPPORTED_ENCODING;
          }
        }
        if (i == length) {
          if (length < bufferSize) {
            if (fieldBufIdx + 1 == fieldBufs.length) {
              fieldBufs[fieldBufIdx] = new FieldBuf(startField, i);
              fieldSize = fieldBufs.length;
              return new NextRecord(i - offset, fieldBufs, LineBreak.NONE);
            } else {
              if (fieldBufIdx == fieldBufs.length) {
                final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                copy[fieldBufIdx] = new FieldBuf(startField, i);
                fieldSize = copy.length;
                return new NextRecord(i - offset, copy, LineBreak.NONE);
              } else {
                final FieldBuf[] copy = new FieldBuf[fieldBufIdx + 1];
                System.arraycopy(fieldBufs, 0, copy, 0, fieldBufIdx);
                copy[fieldBufIdx] = new FieldBuf(startField, i);
                fieldSize = copy.length;
                return new NextRecord(i - offset, copy, LineBreak.NONE);
              }
            }
          } else {
            return NextRecord.BUFFER_UNDERFLOW;
          }
        } else {
          return NextRecord.BUFFER_OVERFLOW;
        }
      }
    }
  
    private static final class NextRecord {
  
      private static final NextRecord BUFFER_UNDERFLOW = new NextRecord(-1);
      private static final NextRecord BUFFER_OVERFLOW = new NextRecord(-2);
      private static final NextRecord INVALID_CSV_FORMAT = new NextRecord(-3);
      private static final NextRecord UNSUPPORTED_ENCODING = new NextRecord(-4);
  
      private final int offset;
      private final FieldBuf[] fields;
      private final LineBreak lineBreak;
      private final int status;
  
      private NextRecord(final int offset, final FieldBuf[] fields, final LineBreak lineBreak) {
        this.offset = offset;
        this.fields = fields;
        this.lineBreak = lineBreak;
        this.status = 0;
      }
  
      private NextRecord(final int error) {
        this.offset = 0;
        this.fields = new FieldBuf[0];
        this.lineBreak = LineBreak.NONE;
        this.status = error;
      }
    }
  
    private enum State {
      START,
      START_QUOTE,
      MUST_QUOTE
    }
  
    private static final class FieldBuf {
  
      private final int start;
      private final int end;
      private final int len;
  
      private FieldBuf(final int start, final int end) {
        this.start = start;
        this.end = end;
        this.len = end - start;
      }
    }
  
    /**
     * CSV Record
     *
     * <p>
     */
    public static final class Record {
  
      private final long number;
      private final String[] fields;
      private final LineBreak lineBreak;
  
      private Record(final long number, final String[] fields, final LineBreak lineBreak) {
        this.number = number;
        this.fields = fields;
        this.lineBreak = lineBreak;
      }
  
      /**
       * Create CSV record with {@code CRLF} line endings.
       *
       * @param fields CSV fields.
       * @return returns new {@link Record}.
       */
      public static Record create(final String... fields) {
        return new Record(-1, fields, LineBreak.CRLF);
      }
  
      /**
       * Create CSV record with specified line endings.
       *
       * @param lineBreak line endings.
       * @param fields CSV fields.
       * @return returns CSV {@link Record} with specified line endings.
       */
      public static Record create(final LineBreak lineBreak, final String... fields) {
        return new Record(-1, fields, lineBreak);
      }
  
      public final long number() {
        return number;
      }
  
      public final String[] fields() {
        return fields;
      }
  
      public final LineBreak lineBreak() {
        return lineBreak;
      }
  
      @Override
      public final boolean equals(final Object o) {
        if (o instanceof Record) {
          final Record record = (Record) o;
          if (fields.length != record.fields.length) {
            return false;
          }
          for (int i = 0; i < fields.length; i++) {
            if (!fields[i].equals(record.fields[i])) {
              return false;
            }
          }
          return number == record.number && lineBreak == record.lineBreak;
        } else {
          return false;
        }
      }
  
      @Override
      public final int hashCode() {
        int result = (int) (number ^ (number >>> 32));
        for (final String element : fields) {
          result = 31 * result + (element == null ? 0 : element.hashCode());
        }
        result = 31 * result + lineBreak.length;
        return result;
      }
  
      @Override
      public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(fields[0]);
        for (int i = 1; i < fields.length; i++) {
          sb.append(',');
          sb.append(fields[i]);
        }
        sb.append(']');
        return String.format(
            "[number=%d, fields=%s, lineBreak=%s]", number, sb.toString(), lineBreak);
      }
    }
  
    /**
     * GigaCSV configuration options.
     *
     * <p>
     */
    public static final class Option {
      private int bufferSize;
      private int headerSize;
      private byte separator;
      private final byte quote;
  
      private Option(
          final int bufferSize, final int headerSize, final byte quote, final byte separator) {
        this.bufferSize = bufferSize;
        this.headerSize = headerSize;
        this.quote = quote;
        this.separator = separator;
      }
  
      /**
       * Create default CSV configuration.
       *
       * <p>
       */
      public Option() {
        this(1 << 16, 1 << 8, (byte) '\"', (byte) ',');
      }
  
      /**
       * Buffer size.
       *
       * @param bufferSize buffer size.
       * @return returns this {@link Option}.
       */
      public final Option bufferSize(final int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
      }
  
      /**
       * Default header size.
       *
       * @param headerSize default header size.
       * @return returns this {@link Option}.
       */
      public final Option headerSize(final int headerSize) {
        this.headerSize = headerSize;
        return this;
      }
  
      /**
       * Set field separator.
       *
       * @param separator field separator.
       * @return returns this {@link Option}.
       */
      public final Option separator(final Separator separator) {
        if (separator == null) {
          this.separator = Separator.COMMA.value;
        } else {
          this.separator = separator.value;
        }
        return this;
      }
    }
  
    /**
     * Line endings.
     *
     * <p>
     */
    public enum LineBreak {
      NONE(0), //
      CR(1), //
      LF(1), //
      CRLF(2); //
  
      private final int length;
  
      private LineBreak(final int length) {
        this.length = length;
      }
    }
  
    /**
     * Field separator.
     *
     * <p>
     */
    public enum Separator {
      BAR((byte) '|'), //
      COMMA((byte) ','); //
  
      private byte value;
  
      private Separator(final byte value) {
        this.value = value;
      }
    }
  }