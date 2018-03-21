# Hodge/Podge

## Convert Java 1.8+ time and date classes to and from legacy time and date classes

This project attempts to provide a comprehensive set of example conversion
methods to translate between any combination of legacy API dates and times and
new API dates and times -- with unit tests.

Perhaps you've got a Calendar and you need a ZonedDateTime. Maybe you're doing
your calculations using Instant objects, but one legacy API still needs a
java.util.Date. This code will hopefully show you how to handle that correctly.
Conversions are performed as directly as possible. None of these conversions are
particularly complicated, but I found half a dozen bugs while adding the unit
tests, so I think it's worthwhile using reference code rather than writing your
own every time.

That said, this code comes with no warranty express or implied, as per the
license. Please perform your own tests before using it for anything important.

<meta@pobox.com>
