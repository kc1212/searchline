# searchline
Binary search on very large newline delimited ASCII files. Inspired by the need to search large password files (30 GB uncompressed) from [haveibeenpwned](https://haveibeenpwned.com/Passwords).

## Limitations
- This software does not work on Windows line-endings.
- The lines must have the same length.
- The search string must have the same length as the line.
