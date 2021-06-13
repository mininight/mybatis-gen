package io.light.frame.dal.mybatis.generator.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * File visitor function
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-10 10:55
 * @see java.nio.file.SimpleFileVisitor
 */
@FunctionalInterface
public interface FileVisitorFunction extends FileVisitor<Path> {

    /**
     * Invoked for a directory before entries in the directory are visited.
     *
     * <p> Unless overridden, this method returns {@link FileVisitResult#CONTINUE
     * CONTINUE}.
     */
    @Override
    default FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs)
            throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(attrs);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Invoked for a file in a directory.
     *
     * <p> Unless overridden, this method returns {@link FileVisitResult#CONTINUE
     * CONTINUE}.
     */
    @Override
    FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException;

    /**
     * Invoked for a file that could not be visited.
     *
     * <p> Unless overridden, this method re-throws the I/O exception that prevented
     * the file from being visited.
     */
    @Override
    default FileVisitResult visitFileFailed(Path path, IOException exc)
            throws IOException {
        Objects.requireNonNull(path);
        throw exc;
    }

    /**
     * Invoked for a directory after entries in the directory, and all of their
     * descendants, have been visited.
     *
     * <p> Unless overridden, this method returns {@link FileVisitResult#CONTINUE
     * CONTINUE} if the directory iteration completes without an I/O exception;
     * otherwise this method re-throws the I/O exception that caused the iteration
     * of the directory to terminate prematurely.
     */
    @Override
    default FileVisitResult postVisitDirectory(Path path, IOException exc)
            throws IOException {
        Objects.requireNonNull(path);
        if (exc != null) {
            throw exc;
        }
        return FileVisitResult.CONTINUE;
    }
}
