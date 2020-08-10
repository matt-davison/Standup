# Standup


## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Standup is a platform that allows its users to moderate the content they see. Jsut like standup comedy, Standup takes a very aggressive approach in how it forces users to vote for content. Content is posted to communities that are designed to help democratize the platform and prevent a few influencers from being the only content creators most people see. 

### App Evaluation
[Evaluation of your app across the following attributes]
   - **Category**: Social Networking/Entertainment
   - **Mobile**: Camera is used to share images and videos. Swipe gestures are used to interact with posts.
   - **Story**: Creates more interaction on posts. Rating is very important for posts to be shown in trending sort.
   - **Market**: Young adults
   - **Habit**: Users will open this app many times in a day- content is very short and when using the "hot" sort only the best new content will be shown. They will also be able to post their own microblog content. The average user will mostly consume but probably create at least once a week.
   - **Scope**: This app will be challenging but it is probably possible to build by the end. A stripped down version would still be entertaining.

### App Expectations
- [x] Your app has multiple views
- [x] Your app interacts with a database (e.g. Parse)
- [x] You can log in/log out of your app as a user
- [x] You can sign up with a new user profile
- [x] Somewhere in your app you can use the camera to take a picture and do something with the picture (e.g. take a photo and share it to a feed, or take a photo and set a user’s profile picture)
- [x] Your app integrates with a SDK (e.g. Google Maps SDK, Facebook SDK)
- [x] Your app contains at least one more complex algorithm (talk over this with your manager)
- [x] Your app uses gesture recognizers (e.g. double tap to like, e.g. pinch to scale)
- [x] Your app use an animation (doesn’t have to be fancy) (e.g. fade in/out, e.g. animating a view growing and shrinking)
- [x] Your app incorporates an external library to add visual polish

### Walkthrough
![Video Walthrough](https://github.com/matt-davison/Standup/blob/master/Kapture.gif)
![Video Walthrough part 2](https://github.com/matt-davison/Standup/blob/master/Kapture-2.gif)
GIFs created with [Kap](https://getkap.co/).

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**
- [x] User can register an account
- [x] User can login
- [x] User can follow communities
- [x] User can create communities
- [x] User can vote for content (swipe left/right)
- [x] User can view post details (swipe up)
  - [x] User can view other user profiles
  - [x] User can view comments
  - [x] User can create comments
- [x] User can sort a feed by trending, top, latest
- [x] User can search for a community
- [x] User can create posts (with or without media)
- [x] User can set profile picture

**Optional Nice-to-have Stories**
- [x] User can create posts with GIFs (bites)
- [ ] User can report content
- [ ] User can change username
- [ ] User can search for posts and other users
- [x] Option to hide already seen posts in feed
- [x] Explore Feed shows suggested communities
- [x] Detail view for a community
- [ ] Save most recently used sorting method
- [ ] View like/dislike history
- [ ] Share posts outside app
- [ ] Share posts within app (to user groups)

### 2. Screen Archetypes

* Register
    * User can register an account
* Login
    * User can login
* Stream
    * User can view home feed
* Explore
    * User can use Explore Feed to find suggested communities
    * User can search for communities
* Creation
    * User can create content
* Profile
    * User can View account details
    * User can view the their posts
* Detail
    * User can comment on posts
    * User can follow communities and view posts in a list

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Stream
* Explore
* Create
* Profile

**Flow Navigation** (Screen to Screen)
* Login
  * Register
  * Home Feed
* Home Feed
  * Profile
* Explore
  * Community Details
    * Post Details
      * Profile Details
* Create
  * Post Details
* Profile
  * Post Details
  * Login
  
## Wireframes

https://www.figma.com/file/lbDroPNd0w1CCVdORwZZ1w/Standup?node-id=0%3A1


## Schema 
### Models
User
| Property      | Type      | Description        |
|---------------|-----------|--------------------|
|objectId|String|User's id|
|username|String|The User's username|
|email|String|The User's email address|
|password|String|The User's password|
|communities|Relation to Community|The communities a User follows|
|tagHistory|JSONObject|The User's tag history and preferences [{tagId, likes, views}]|
|likeHistory|Relation to Post|The posts a user has liked|
|viewHistory|Relation to Post|The posts a user has liked|

Post
| Property      | Type      | Description        |
|---------------|-----------|--------------------|
|objectId|String|Post's id|
|author|Pointer to User|The author|
|createdAt|Date|When the post was created|
|title|String|The Post's title|
|media|File|The Post's attached media|
|description|The Posts's description|
|rating|Number|The Post's rating|
|likes|Number|The Post's likes|
|views|Number|The Post's views|
|viewers|Relation to User|Users that viewed this post|
|postedTo|Relation to Community|Communities this post is shared to|
|comments|Relation to Comment|Comments on this post|

Community
| Property      | Type      | Description        |
|---------------|-----------|--------------------|
|objectId|String|The Community's id|
|name|String|The Community's name|
|description|String|The Community's description|
|createdAt|Date|When the Community was created|
|banner|File|The Community's banner|
|icon|File|The Community's icon|
|mods|Relation to User|The Community's mods|
|userCount|Number|The number of followers|
|banned|Relation to User|Users banned from this Community|
|tags|Relation to Tag|Tags for this Community|

Tag
| Property      | Type      | Description        |
|---------------|-----------|--------------------|
|objectId|String|Tag's id|
|tag|String|The tag|
|postsCount|Number|Number of Posts using this tag|

Comment
| Property      | Type      | Description        |
|---------------|-----------|--------------------|
|objectId|String|Comment's id|
|comment|String|The tag|
|author|Pointer to User|The author|
|likes |Number|Number of likes on the comment|

### Networking
Home Feed Screen
- (Read/GET) Query new posts
- (Create/POST) Create a new like on a post
- (Create/POST) Create a new view on a post
- (Create/POST) Create a new comment on a post
Explore Screen
- (Read/GET) User's followed communities
- (Read/GET) Query recommended communities
- (Read/GET) Search community
- (Create/POST) Create community
- (Update/PUT) Follow a community
Create Post Screen
- (Create/POST) Create a new post object
Profile Screen
- (Read/GET) Query logged in user object
- (Update/PUT) Update user profile image
- (Read/GET) Query all posts where user is author
