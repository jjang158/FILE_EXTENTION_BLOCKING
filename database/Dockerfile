FROM postgres:15-alpine

# Copy initialization scripts
COPY init.sh /docker-entrypoint-initdb.d/

# Fix line endings (CRLF to LF) for Windows compatibility and make executable
RUN chmod +x /docker-entrypoint-initdb.d/init.sh && \
    sed -i 's/\r$//' /docker-entrypoint-initdb.d/init.sh
