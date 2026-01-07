import { useState, useEffect } from "react";
import ApiClient from "../service/ApiClient";
import {
  Box,
  Heading,
  Text,
  Table,
  Spinner,
  HStack,
  Card
} from "@chakra-ui/react";

interface Activity {
  id: number;
  name: string;
  distance: number;
  total_elevation_gain: number;
  moving_time: number;
  elapsed_time: number;
  start_date_local: string;
}

interface TopActivitiesWidgetProps {
  title: string;
  action: "distance" | "elevation";
  selectedActivityType: string;
  athleteProfile: any;
}

const formatDate = (dateString: string) => {
  if (!dateString) return "";
  const date = new Date(dateString);
  const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
  const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
  
  const day = String(date.getDate()).padStart(2, '0');
  const month = months[date.getMonth()];
  const year = date.getFullYear();
  const dayName = days[date.getDay()];
  
  let hours = date.getHours();
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const ampm = hours >= 12 ? 'PM' : 'AM';
  hours = hours % 12;
  hours = hours ? hours : 12;
  const hoursStr = String(hours).padStart(2, '0');
  
  // Format: DD MMM, YYYY, ddd HH:mm A (matching Scala moment.js format)
  return `${day} ${month}, ${year}, ${dayName} ${hoursStr}:${minutes} ${ampm}`;
};

const formatElapsedTime = (seconds: number) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  if (hours > 0) {
    return `${hours}h ${minutes}m`;
  }
  return `${minutes}m`;
};

const TopActivitiesWidget = ({
  title,
  action,
  selectedActivityType,
  athleteProfile
}: TopActivitiesWidgetProps) => {
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchActivities = async () => {
      try {
        setLoading(true);
        const data = await ApiClient.topActivities(action, selectedActivityType);
        setActivities(data || []);
      } catch (error) {
        console.error(`Error fetching top ${action} activities:`, error);
        setActivities([]);
      } finally {
        setLoading(false);
      }
    };

    fetchActivities();
  }, [action, selectedActivityType]);

  const distanceUnit = athleteProfile?.unit?.distanceLabel || 'km';
  const elevationUnit = athleteProfile?.unit?.elevationLabel || 'm';

  return (
    <Card.Root>
      <Card.Body>
        <Heading size="md" mb={4} textAlign="center">
          {title}
        </Heading>
        {loading ? (
          <HStack justify="center" py={8}>
            <Spinner />
            <Text>Loading activities...</Text>
          </HStack>
        ) : activities.length === 0 ? (
          <Text textAlign="center" color="gray.500" py={8}>
            No activities found
          </Text>
        ) : (
          <Box overflowX="auto">
            <Table.Root 
              variant="outline" 
              size="sm"
              striped={true}
            >
              <Table.Header>
                <Table.Row>
                  <Table.ColumnHeader fontSize="xs" py={2}>Date</Table.ColumnHeader>
                  <Table.ColumnHeader fontSize="xs" py={2}>Name</Table.ColumnHeader>
                  <Table.ColumnHeader fontSize="xs" py={2}>Distance</Table.ColumnHeader>
                  <Table.ColumnHeader fontSize="xs" py={2}>Elevation</Table.ColumnHeader>
                  <Table.ColumnHeader fontSize="xs" py={2}>Elapsed</Table.ColumnHeader>
                </Table.Row>
              </Table.Header>
              <Table.Body>
                {activities.map((activity) => (
                  <Table.Row key={activity.id}>
                    <Table.Cell fontSize="xs" py={2}>
                      <Box
                        style={{
                          display: '-webkit-box',
                          WebkitLineClamp: 2,
                          WebkitBoxOrient: 'vertical',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          lineHeight: '1.25rem',
                          maxHeight: '2.5rem',
                        }}
                      >
                        <a
                          href={`https://www.strava.com/activities/${activity.id}`}
                          target="_blank"
                          rel="noopener noreferrer"
                          style={{ color: '#0066cc', textDecoration: 'underline', fontSize: '0.75rem' }}
                        >
                          {formatDate(activity.start_date_local)}
                        </a>
                      </Box>
                    </Table.Cell>
                    <Table.Cell fontSize="xs" py={2}>
                      <Box
                        style={{
                          display: '-webkit-box',
                          WebkitLineClamp: 2,
                          WebkitBoxOrient: 'vertical',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          lineHeight: '1.25rem',
                          maxHeight: '2.5rem',
                        }}
                      >
                        <a
                          href={`https://www.strava.com/activities/${activity.id}`}
                          target="_blank"
                          rel="noopener noreferrer"
                          style={{ color: '#0066cc', textDecoration: 'underline', fontSize: '0.75rem' }}
                        >
                          {activity.name}
                        </a>
                      </Box>
                    </Table.Cell>
                    <Table.Cell fontSize="xs" py={2}>
                      {(activity.distance / 1000).toFixed(1)} {distanceUnit}
                    </Table.Cell>
                    <Table.Cell fontSize="xs" py={2}>
                      {Math.round(activity.total_elevation_gain)} {elevationUnit}
                    </Table.Cell>
                    <Table.Cell fontSize="xs" py={2}>
                      {formatElapsedTime(activity.moving_time)} / {formatElapsedTime(activity.elapsed_time)}
                    </Table.Cell>
                  </Table.Row>
                ))}
              </Table.Body>
            </Table.Root>
          </Box>
        )}
      </Card.Body>
    </Card.Root>
  );
};

export default TopActivitiesWidget;

