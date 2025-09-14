# Velocorner Frontend

This is the React-based frontend for Velocorner, a cycling activity analytics platform. The frontend has been decoupled from the original Scala Play framework and now runs as a separate React application that communicates with the backend via REST APIs.

## Features

- **User Authentication**: OAuth2 integration with Strava
- **Activity Analytics**: View personal cycling statistics and achievements
- **Activity Search**: Search through your activities by title, description, or location
- **Product Comparison**: Compare prices across different marketplaces
- **Demo Mode**: View sample statistics for non-authenticated users

## Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── Header.jsx      # Navigation header
│   ├── Footer.jsx      # Site footer
│   ├── Logo.jsx        # Velocorner logo
│   ├── OAuth2Popup.tsx # OAuth2 authentication popup
│   └── ...
├── pages/              # Page components
│   ├── Home.jsx        # Main dashboard
│   ├── Search.jsx      # Activity search
│   ├── Brands.jsx      # Brand search
│   ├── Best.jsx        # Best prices
│   └── About.jsx       # About page
├── service/            # API client and services
│   └── ApiClient.js    # REST API client
├── icons/              # Custom icons
└── App.jsx             # Main application component
```

## API Integration

The frontend communicates with the Scala Play backend through a comprehensive API client that includes:

### Authentication Endpoints
- OAuth2 token exchange with Strava
- JWT token management

### Activity Endpoints
- Activity search and suggestions
- Activity statistics (yearly, YTD, daily)
- Activity types and years
- Word cloud generation

### Statistics Endpoints
- Profile statistics
- Yearly heatmaps
- Distance and elevation histograms
- Top activities
- Achievements

### Demo Endpoints
- Sample data for non-authenticated users

## Getting Started

### Installation

2. Set up environment variables:
```bash
# Create .env file
VITE_API_HOST=https://velocorner.com
```

### Development

Start the development server:
```bash
npm start
```

The app will be available at `http://localhost:3000`

### Building for Production

Build the production bundle:
```bash
npm run build
```

### Environment Configuration

The frontend can be configured for different environments:

- **Production**: `npm start` - Uses remote backend
- **Local**: `npm run local` - Uses localhost:9001 backend

## Authentication Flow

1. User clicks "Connect with Strava"
2. OAuth2 popup opens with Strava authorization
3. User authorizes the application
4. Backend exchanges code for access token
5. Frontend stores JWT token in localStorage
6. API calls include Authorization header with JWT

## Key Components

### Home Page
- System status display
- Strava authentication
- User statistics dashboard
- Activity type selection
- Word cloud visualization
- Demo mode for non-authenticated users

### Search Page
- Activity search functionality
- Real-time search suggestions
- Detailed activity results
- Distance, duration, and elevation display

## Deployment

The frontend is designed to be deployed as a static application:

1. Build the production bundle
2. Serve static files from a web server
3. Configure API proxy or CORS for backend communication

### Docker Deployment

A Dockerfile is included for containerized deployment:

```bash
# for Intel
docker build -t velocorner.frontend .
# for ARM
docker build -t velocorner.frontend --platform linux/arm64 .

# Intel/ARM
docker run -p 8080:3000 peregin/velocorner.frontend:latest
# ARM - build and push
docker buildx build --platform linux/amd64 -t peregin/velocorner.frontend:latest --cache-from "type=registry,ref=peregin/velocorner.frontend:latest" --cache-to "type=inline" --push .
```

## Backend Integration

This frontend is designed to work with the Scala Play backend (`web-app` module). The backend provides:

- REST API endpoints
- OAuth2 token management
- Data processing and analytics
- Database access
- External service integration (Strava, marketplaces)

## Contributing

1. Follow the existing code style
2. Use TypeScript for new components
3. Include proper error handling
4. Test API integration
5. Update documentation as needed

## License

MIT License - see LICENSE file for details