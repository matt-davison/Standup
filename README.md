# Standup


## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Standup is a platform that allows its users to moderate the content they see. Just like standup comedy, content that people like gets more attention and content they don't "sits down" (stops being shown in feeds). Users can only navigate through content by swiping up/down to signal they liked/disliked it. Content can be interacted with by swiping up to see comments and details about the community it came from. Content can be reported by swiping down. Standup content is posted to two types of communities: me/ and up/. me/ communities are controlled by an individual and the individual account holder is the only one that can post to their personal community. up/ communities allow any follower to post to them and are a great way to get your content seen by those with similar interests. This system allows even the smallest creators to have a chance at stardom - if you post high quality content that is fitting to a community, people will start to follow your personal community!

### App Evaluation
[Evaluation of your app across the following attributes]
   - **Category**: Social Networking/Entertainment
   - **Mobile**: Camera is used to share images and videos. Push notifications when people interact with your content.
   - **Story**: Creates more interaction on posts.  Disliked content will be taken down. 
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
GIF created with [Kap](https://getkap.co/).

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**
- [x] User can register an account
- [x] User can login
- [x] User can follow communities
- [] User can create communities
- [x] User can vote for content (swipe left/right)
- [x] User can view post details (swipe up)
  - [] User can view author's profile
  - [] User can view comments
  - [] User can create comments
- [x] User can sort a feed by trending, top, latest
- [x] User can search for a community
- [x] User can create posts (with or without media)
- [x] User can set profile picture

**Optional Nice-to-have Stories**
- [] User can create posts with GIFs (bites)
- [] User can report content
- [] User can change username
- [] User can view other users' profiles
- [] User can search for posts and other users
- [] Option to hide already seen posts in feed
- [] Explore Feed to find collaborative-filtering suggested content
- [] Can filter Search by creator or community
- [x] Detail view for a community
- [] Save most recently used sorting method
- [] View like/dislike history
- [] Share posts outside app
- [] Share posts within app (to user groups)

### 2. Screen Archetypes

* Register
    * User can register an account
* Login
    * User can login
* Stream
    * User can view home feed
    * User can View a community's feed
    * User can Follow a community
    * User can sort feed
* Search
    * User can Find a community
    * User can Create a community
    * User can Follow a community
* Explore
    * User can use Explore Feed to find suggested content
    * User can Follow a community
* Creation
    * User can create content
* Profile
    * User can view settings menu
    * User can View account details
    * User can View me/ community details
    * User can View up/ community details
* Detail
    * User can interact with content
    * User can go to View me/ community details
    * User can go to View up/ community details

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Stream
* Explore
* Create
* Activity
* Profile

**Flow Navigation** (Screen to Screen)

* Register
    * Explore
    * Search
    * Stream
        * Detail
* Login
    * Stream
        * Detail
* Stream
    * Detail
    * Profile
* Explore
    * Stream
        * Detail
    * Search
        * Stream
* Creation
    * Profile
* Profile
    * Settings
    * Detail
    * Stream
* Detail
    * Profile

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
|followers|Number|The number of followers|
|followings|Number|The number of followings|
|following|Relation to User|The Users this User is following|
|tagHistory|JSONObject|The User's tag history and preferences [{tagId, likes, views}]|
|likeHistory|Relation to Post|The posts a user has liked|

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
- (Read/GET) Query recommended posts
- (Read/GET) Query for specific community
Create Post Screen
- (Create/POST) Create a new post object
Profile Screen
- (Read/GET) Query logged in user object
- (Update/PUT) Update user profile image
- (Update/PUT) Update user username
- (Read/GET) Query all posts where user is author
- (Read/GET) Query followers
- (Read/GET) Query following
