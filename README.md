# mPower-2-Android
Source code for the Android version of the mPower 2 study app.


# Study Bursts

## Study burst SMS reminders
Preconditions:
- Account has verified phone number
- Activities must have been requested
- Time zone between UTC-11 and UTC-1
- Account is in consented state

When the participant misses 3 consecutive days or missed 4 days total for taskId `study-burst-task`, the day before their next burst, they receive an SMS.

Calculation for missed days is done on the server, so manipulating the clock on a mobile device is insufficient.
