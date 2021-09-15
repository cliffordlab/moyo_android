## Table of Contents
1. [About the study](#1-about-the-study)
2. [About the data](#2-about-the-data)
    1. [Activity](#21-activity) 
    2. [Location Context](#22-location-context) 
3. [User signup and sign in](#3-user-signup-and-sign-in)
    1. [Application Adminstrator Site](#31-application-adminstrator-site)
    2. [Mobile Application User Sign in](#32-mobile-application-user-sign-in)
4. [Data Format](#4-data-format)


# 1. About the study

* Amoss | Joint study with Emory, Morehouse and others 
* Community driven app with goal to improve health


# 2. About the data
The data collected by the AMoSS mobile app is information about activity, location context, and social networking.


## 2.1. Activity
1. [Accelerometer](#accelerometer)

## **Accelerometer**
We collect data from the accelerometer sensor in the form. The data is given to us with each sample having a time,
x coordinate, y coordinate, and z coordinate. The coordinates changes according to the acceleration forces measured
against those coordinates.

## 2.2. Location Context
1. [Phone Location](#accelerometer)
2. [Google Location Context](#google_api_location_context)

## **Phone Location**
Phone location is retrieved using the android location services api. We get location based off the availability of cell phone towers and wifi access points. This data is collected with a specific offset to de-identify the data collected.

## **Google Api Location Context**
Google gives us api's to access some of the the vast amount of data that they are able to collect. Using these resources we collect information about the places a user has been near as well as the weather a user is experiencing. For example if a user is walking by a Wendy's the context data we will collect can give us a timestamp, name, address, establishment number, and the likelihood that the user was actually in that location. A weather data sample would give us timestamp, temperature, feels like, dew, humidity, and condition number.

# 3. User signup and sign in 

## 3.2. Mobile Application User Sign in

### User Sign in
When the application is first started up, the initial screen is login has fields for "participant id" and "password." 

# 4. Data Format 

**API** storage path in s3 will be **"bucketSS/study/10-digit-id/startOfWeek millis/data_file"** the subfolder will be created every week set by whenever a users phone passing the threshold of monday at 10am for whatever timezone they are currently in

## 4.1. Accelerometer file
1. <b>File Name:</b> unix_time_stamp with 1 digit dropped with .acc extension (example: 495597707.acc)
2. <b>Header:</b> "time,x,y,z"

## 4.2. Location file
1. <b>File Name:</b> offset on location 20 degress on long and lat .loc extension (example: location.loc) will need to change to 495597707.loc because sub folders are change to weekly every monday at 10am instead of daily
2. <b>Header:</b> "time,latitude,longitude"

## 4.3. Weight file
1. <b>File Name:</b> .wt extension (example: weight.wt) for same reason as location file will change to 495597707.weight
2. <b>Header:</b> "weight/lbs,date"

## 4.4. Charging file
1. <b>File Name:</b> will updated every time a user charge to phone .txt extension (example: charging.txt) for same reason as location file will change to 495597707.charging
2. <b>Header:</b> "is_charging,time"

## 4.5. Sms file
1. <b>File Name:</b> counts how many time a message has words from a category in liwc .sms extension (example: socact.sms) for same reason as location file will change to 495597707.sms
2. <b>Header:</b> "category/meta=value"
3. <b>Key Info:</b> for key "type" the values are 

Value | Meaning |
--- | --- |
0 | type all
1 | type inbox
2 | type sent
3 | type draft
4 | type outbox
5 | type failed
6 | type queued

## 4.7. Call file
1. <b>File Name:</b> .call extension (example: socact_call.call) for same reason as location file will change to 495597707.call
2. <b>Header:</b> "hashed-ph-number,call-type,call-date,call-duration"
3. Call duration is in seconds

## 4.8. Places file
1. <b>File Name:</b> make requests to google api's for location context information such as restaurants and whatever establishments are around .csv extension (example: Places.csv) for same reason as location file will change to 495597707.places
2. <b>Header:</b> "Timestamp,Name,Address,Estab.#,Likelihood"

## 4.9. Weather file
1. <b>File Name:</b> make requests to google api's for weather information .csv extension (example: Weather.csv) for same reason as location file will change to 495597707.weather
2. <b>Header:</b> "Timestamp,Temperature,Feels Like,Dew,Humidity,Cond#"

## 4.10. Survey files
File extensions | type |
--- | --- |
mz | mood zoom survey
ms | mood swipe survey
acc | accelerometer
phq | phq9 survey
sms | default sms app data
call | call data
weather | google api weather
places | google api places
weight | participants inputted weight
charge | every time participant plugs in or out phone
dismiss | when participant dismissed a survey notification
tz | records changes in timezone
kccq | kccq survey

###**Mood Zoom**
Filename e.g. `495597707.mz`

`Header Values` | Question | Answer  |
--- | --- | --- |
1 | Anxious | 1-7 low to high
2 | Elated | 1-7 low to high
3 | Sad | 1-7 low to high
4 | Angry | 1-7 low to high
5 | Irritable | 1-7 low to high
6 | Energetic | 1-7 low to high
7 | What was the main cause of your stress today? | 0-6

Mood Zoom Question 6  `#` | Answer |
--- | --- |
0 | no answer
1 | health
2 | work/study
3 | money
4 | relationship
5 | family
6 | other
Example File:

```
0,1,2,3,4,5,6
1,1,1,1,2,3,4
```

If multiple answers or chosen input will have multiple numbers e.g. `123`

###**Mood Swipe**

Filename e.g. `495597707.ms`

Mood Swipe Key | MoodSwipe Value |
--- | --- |
1 | angry
2 | sad
3 | neutral
4 | happy
5 | excited

Example File:

```
Mood
3
```

###**PHQ9**

Filename e.g. `495597707.phq`

 Question Key | Question Value |
--- | --- |
0 | Little interest or pleasure in doing things
1 | Feeling down, depressed, or hopeless
2 | Trouble falling or staying asleep, or sleeping too much
3 | Feeling tired or having little energy
4 | Poor appetite or overeating
5 | Feeling bad about yourself or that you are a failure or have let yourself or your family down
6 | Trouble concentrating on things, such as reading the newspaper or watching television
7 | Moving or speaking so slowly that other people could have noticed? Or the opposite — being so fidgety or restless that you have been moving around a lot more than usual
8 | Thoughts that you would be better off dead or of hurting yourself in some way

Answer Key | Answer Value |
--- | --- |
0 | Not at all
1 | Several days
2 | More than half the days
3 | Nearly every day

Example File:

```
Question,Answer
0,1
1,2
2,3
3,0
4,0
5,0
6,2
7,1
8,1
```

###**KCCQ**

Filename e.g. `495597707.kccq`

 Question Key | Question Value |
--- | --- |
1 | Heart failure affects different people in different ways. Some feel shortness of breath while others feel fatigue. Please indicate how much you are limited by heart failure (shortness of breath or fatigue) in your ability to do the following activities over the past 2 weeks.
2 | Over the past 2 weeks, how many times did you have swelling in your feet, ankles or legs when you woke up in the morning?
3 | Over the past 2 weeks, on average, how many times has fatigue limited your ability to do what you wanted?
4 | Over the past 2 weeks, on average, how many times has shortness of breath limited your ability to do what you wanted?
5 | Over the past 2 weeks, on average, how many times have you been forced to sleep sitting up in a chair or with at least 3 pillows to prop you up because of shortness of breath?
6 | Over the past 2 weeks, how much has your heart failure limited your enjoyment of life?
7 | If you had to spend the rest of your life with your heart failure the way it is right now, how would you feel about this?
8 | How much does your heart failure affect your lifestyle? Please indicate how your heart failure may have limited your participation in the following activities over the past 2 weeks.

Answer Key Q1 | Answer Value Q1 |
--- | --- |
1 | Extremely Limited
2 | Quite a bit Limited
3 | Moderately Limited
4 | Slightly Limited
5 | Not at all Limited
6 | Limited for other reasons or did not do the activity

Answer Key Q2 | Answer Value Q2 |
--- | --- |
1 | Every Morning
2 | 3 or more times per week but not every day
3 | 1-2 times per week
4 | Less than once a week
5 | Never over the past 2 weeks

Answer Key Q3-4 | Answer Value Q3-4 |
--- | --- |
1 | All of the time
2 | Severak times per day
3 | At least once a day
4 | 3 or more times per week but not every day
5 | 1-2 times per week
6 | Less than once a week
7 | Never over the past 2 weeks

Answer Key Q5 | Answer Value Q5 |
--- | --- |
1 | Every night
2 | 3 or more times per week but not every day
3 | 1-2 times per week
4 | Less than once a week
5 | Never over the past 2 weeks

Answer Key Q6 | Answer Value Q6 |
--- | --- |
1 | Every night
2 | 3 or more times per week but not every day
3 | 1-2 times per week
4 | Less than once a week
5 | Never over the past 2 weeks

Answer Key Q7 | Answer Value Q7 |
--- | --- |
1 | Not at all satisfied
2 | Mostly dissatisfied
3 | Somewhat satisfied
4 | Mostly satisfied
5 | Completed satisfied

Answer Key Q8 | Answer Value Q8 |
--- | --- |
1 | Severly Limited
2 | Limited Quite a bit 
3 | Moderately Limited
4 | Slightly Limited
5 | Did not limit at all
6 | Does not apply or did not do for other reasons

Example File:

```
1,134
2,2
3,3
4,5
5,5
6,4
7,2
8,511
```

Privacy Policy:
The Moyo Health Network provised this application as a Free app. This SERVICE is provided by the Moyo Health Network at no cost and is intended for use as is.
This page is used to inform visitors regarding my policies with the collection, use, and disclosure of Personal Information if anyone decided to use my Service.
If you choose to use my Service, then you agree to the collection and use of information in relation to this policy. The Personal Information that I collect is used for providing and improving the Service. I will not use or share your information with anyone except as described in this Privacy Policy.
The terms used in this Privacy Policy have the same meanings as in our Terms and Conditions, which is accessible at Moyo Health Network unless otherwise defined in this Privacy Policy.
Information Collection and Use
For a better experience, while using our Service, I may require you to provide us with certain personally identifiable information, including but not limited to Moyo Health Network collects user entered photos, vital signs and mood survey scores but this information is not tied to any identifiable information. Users are identified internally by a series of randomly generated log in credentials and no information regarding name, date of birth, etc. are collected.
Log Data
I want to inform you that whenever you use my Service, in a case of an error in the app I collect data and information (through third party products) on your phone called Log Data. This Log Data may include information such as your device Internet Protocol (“IP”) address, device name, operating system version, the configuration of the app when utilizing my Service, the time and date of your use of the Service, and other statistics.
Cookies
Cookies are files with a small amount of data that are commonly used as anonymous unique identifiers. These are sent to your browser from the websites that you visit and are stored on your device's internal memory.
This Service does not use these “cookies” explicitly. However, the app may use third party code and libraries that use “cookies” to collect information and improve their services. You have the option to either accept or refuse these cookies and know when a cookie is being sent to your device. If you choose to refuse our cookies, you may not be able to use some portions of this Service.
Service Providers
I may employ third-party companies and individuals due to the following reasons:
* To facilitate our Service;
* To provide the Service on our behalf;
* To perform Service-related services; or
* To assist us in analyzing how our Service is used.
I want to inform users of this Service that these third parties have access to your Personal Information. The reason is to perform the tasks assigned to them on our behalf. However, they are obligated not to disclose or use the information for any other purpose.
Security
I value your trust in providing us your Personal Information, thus we are striving to use commercially acceptable means of protecting it. But remember that no method of transmission over the internet, or method of electronic storage is 100% secure and reliable, and I cannot guarantee its absolute security.
Links to Other Sites
This Service may contain links to other sites. If you click on a third-party link, you will be directed to that site. Note that these external sites are not operated by me. Therefore, I strongly advise you to review the Privacy Policy of these websites. I have no control over and assume no responsibility for the content, privacy policies, or practices of any third-party sites or services.
Children’s Privacy
These Services do not address anyone under the age of 18. I do not knowingly collect personally identifiable information from children under 18 years of age. In the case I discover that a child under 18 has provided me with personal information, I immediately delete this from our servers. If you are a parent or guardian and you are aware that your child has provided us with personal information, please contact me so that I will be able to do necessary actions.
Changes to This Privacy Policy
I may update our Privacy Policy from time to time. Thus, you are advised to review this page periodically for any changes. I will notify you of any changes by posting the new Privacy Policy on this page.
This policy is effective as of 2021-06-22
Contact Us
If you have any questions or suggestions about my Privacy Policy, do not hesitate to contact me at clabdevelopment@gmail.com.
This privacy policy page was created at privacypolicytemplate.net and modified/generated by App Privacy Policy Generator
