# searchline
Binary search on very large newline delimited ASCII files.
Inspired by the need to search large password files (30 GB uncompressed) from [haveibeenpwned](https://haveibeenpwned.com/Passwords).

## Build and run
```
sbt package
scala target/scala-2.12/searchline_2.12-0.1.jar pwned_passwords.txt 5BAA61E4C9B93F3F0682250B6CF8331B7EE68FD8
```

## Performance
On a 30 GB file, searching normally using tools such as `ag` takes roughly 3 minutes to find a match on average.
Using this software it takes less than a second, even when there are no matches.

## Limitations
- This software does not work on Windows line-endings.
- The file must only contain ASCII characters.
- The lines must have the same length (WIP).
- The search string must have the same length as the line (WIP).
