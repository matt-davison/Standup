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

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can register an account
* User can login
* User can follow Communities
* User can create Communities
* User can vote for content to Sit Down or Stand (swipe left or right)
* User can report content (swipe down)
* User can interact with content/creator (swipe up)
* User can view a specific Community's feed
* User can sort a feed by Hot, Best, New
* User can Search for a community
* User can create content
    * Short looping videos (Bites)
    * Pictures
    * Text Post

**Optional Nice-to-have Stories**

* User can filter home feed by up/ communities or me/ communities
* User has settings menu
    * Change name
    * Change privacy of posts made to your personal community
* Explore Feed to find new, suggested content
* Can filter Search by creator or community
* User can create livestreams (cannot be posted to a community and are displayed at top of home feed and on user's page)
* Detail view for a community


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
|phone|String|The User's phone number|
|communities|Array<Pointer to Community>|The communities a User follows|
|following|Array<Pointer to User>|The Users a user follows|
|followers|Array<Pointer to User>|The Users following this user|
|posts|Array<Pointer to Post>|The User's posts|
|tagHistory|JSONObject|The User's tag history and preferences [{tagId, likes, views}]|

Post
| Property      | Type      | Description        |
|---------------|-----------|--------------------|
|objectId|String|Post's id|
|author|Pointer to User|The author|
|createdAt|Date|When the post was created|
|updatedAt|Date|When the post was updated|
|postedTo|Array<Pointer to Community>|Where the post is shared|
|title|String|The Post's title|
|media|File|The Post's attached media|
|description|The Posts's description|
|rating|Number|The Post's rating|
|views|Number|The Post's views|
|tags|Array<Pointer to Tag>|The Post's tags|

Community
| Property      | Type      | Description        |
|---------------|-----------|--------------------|
|objectId|String|The Community's id|
|name|String|The Community's name|
|description|String|The Community's description|
|createdAt|Date|When the Community was created|
|banner|File|The Community's banner|
|icon|File|The Community's icon|
|mods|Array<Pointer to User>|The Community's mods|
|userCount|Number|The number of followers|
|banned|Array<Pointer to User>|Users banned from this Community|
|posts|Array<Pointer to Post>|Posts made to this Community|
|tags|Array<Pointer to Tag>|Tags for this Community|
   
Tag
| Property      | Type      | Description        |
|---------------|-----------|--------------------|
|objectId|String|Tag's id|
|tag|String|The tag|
|communities|Array<Pointer to Community>|Communities with this tag|
|posts|Array<Pointer to Post>|Posts using this tag|
|postsCount|Number|Number of Posts using this tag|


### Networking
- [Add list of network requests by screen ]
- [Create basic snippets for each Parse network request]
